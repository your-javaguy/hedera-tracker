package com.hedera.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.tracker.model.StockQuote;
import com.hedera.tracker.repository.StockQuoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StockMarketService {

    @Autowired
    @Qualifier("stockApiRestTemplate")
    private RestTemplate restTemplate;
    
    @Autowired
    private String stockApiBaseUrl;
    
    @Autowired
    private String stockApiKey;
    
    @Autowired
    private List<String> defaultStockSymbols;
    
    @Autowired
    private StockQuoteRepository stockQuoteRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Fetches current stock quote for a symbol
     * @param symbol the stock symbol (e.g., AAPL, MSFT)
     * @return StockQuote object with current data
     */
    public StockQuote fetchStockQuote(String symbol) {
        int maxRetries = 3;
        int retryCount = 0;
        int retryDelayMs = 2000; // Start with 2 second delay
        
        while (retryCount < maxRetries) {
            try {
                String url = UriComponentsBuilder.fromUriString(stockApiBaseUrl)
                    .queryParam("function", "GLOBAL_QUOTE")
                    .queryParam("symbol", symbol)
                    .queryParam("apikey", stockApiKey)
                    .build()
                    .toUriString();
                
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);
                JsonNode quote = root.path("Global Quote");
                
                if (quote.isMissingNode() || quote.isEmpty()) {
                    log.warn("No quote data found for symbol: {}, attempt {}/{}", symbol, retryCount + 1, maxRetries);
                    retryCount++;
                    if (retryCount < maxRetries) {
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2; // Exponential backoff
                        continue;
                    }
                    return null;
                }
                
                BigDecimal price = new BigDecimal(quote.path("05. price").asText("0"));
                BigDecimal change = new BigDecimal(quote.path("09. change").asText("0"));
                BigDecimal changePercent = new BigDecimal(
                    quote.path("10. change percent").asText("0%").replace("%", "")
                );
                
                // Get company info for additional details
                String companyName = fetchCompanyName(symbol);
                
                StockQuote stockQuote = StockQuote.builder()
                    .symbol(symbol)
                    .companyName(companyName)
                    .price(price)
                    .change(change)
                    .changePercent(changePercent)
                    .volume(Long.parseLong(quote.path("06. volume").asText("0")))
                    .lastUpdated(LocalDateTime.now())
                    .build();
                
                // Save to database
                stockQuoteRepository.save(stockQuote);
                
                return stockQuote;
            } catch (Exception e) {
                log.error("Error fetching stock quote for symbol {}: {} (attempt {}/{})", 
                    symbol, e.getMessage(), retryCount + 1, maxRetries);
                retryCount++;
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Fetches company name and additional info
     */
    private String fetchCompanyName(String symbol) {
        try {
            String url = UriComponentsBuilder.fromUriString(stockApiBaseUrl)
                .queryParam("function", "OVERVIEW")
                .queryParam("symbol", symbol)
                .queryParam("apikey", stockApiKey)
                .build()
                .toUriString();
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            return root.path("Name").asText(symbol);
        } catch (Exception e) {
            log.error("Error fetching company info for symbol {}: {}", symbol, e.getMessage());
            return symbol;
        }
    }
    
    /**
     * Gets the most recent stock quote from the database
     * @param symbol the stock symbol
     * @return the most recent StockQuote or null if not found
     */
    public StockQuote getLatestQuote(String symbol) {
        Optional<StockQuote> quote = stockQuoteRepository.findTopBySymbolOrderByLastUpdatedDesc(symbol);
        return quote.orElse(null);
    }
    
    /**
     * Gets all tracked stock quotes (latest for each symbol)
     * @return list of latest quotes for all tracked symbols
     */
    public List<StockQuote> getAllLatestQuotes() {
        List<StockQuote> latestQuotes = new ArrayList<>();
        
        for (String symbol : defaultStockSymbols) {
            StockQuote quote = getLatestQuote(symbol);
            if (quote == null) {
                // If no quote in database, fetch from API
                quote = fetchStockQuote(symbol);
            }
            
            if (quote != null) {
                latestQuotes.add(quote);
            }
        }
        
        return latestQuotes;
    }
    
    /**
     * Gets top performing stocks
     * @return list of stocks with highest percentage gains
     */
    public List<StockQuote> getTopGainers() {
        return stockQuoteRepository.findTop5ByOrderByChangePercentDesc();
    }
    
    /**
     * Gets worst performing stocks
     * @return list of stocks with highest percentage losses
     */
    public List<StockQuote> getTopLosers() {
        return stockQuoteRepository.findTop5ByOrderByChangePercentAsc();
    }
    
    /**
     * Gets most active stocks by volume
     * @return list of stocks with highest trading volume
     */
    public List<StockQuote> getMostActiveStocks() {
        return stockQuoteRepository.findTop5ByOrderByVolumeDesc();
    }
    
    /**
     * Scheduled task to update stock quotes for all tracked symbols
     */
    @Scheduled(fixedRate = 900000) // Every 15 minutes (changed from 5 minutes)
    public void updateAllStockQuotes() {
        log.info("Updating stock quotes for {} symbols", defaultStockSymbols.size());
        
        for (String symbol : defaultStockSymbols) {
            try {
                fetchStockQuote(symbol);
                // Add larger delay to avoid API rate limits
                Thread.sleep(3000);
            } catch (Exception e) {
                log.error("Error updating quote for {}: {}", symbol, e.getMessage());
            }
        }
        
        log.info("Stock quotes update completed");
    }
} 