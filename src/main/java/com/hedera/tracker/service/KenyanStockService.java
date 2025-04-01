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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KenyanStockService {

    @Autowired
    private StockQuoteRepository stockQuoteRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // A map of Kenyan stock symbols and their company names
    private final Map<String, String> kenyanStocks = new HashMap<>();
    
    // Initialize with some common Kenyan stocks
    public KenyanStockService() {
        kenyanStocks.put("KCB", "KCB Group Plc");
        kenyanStocks.put("EABL", "East African Breweries Ltd");
        kenyanStocks.put("SCOM", "Safaricom Plc");
        kenyanStocks.put("EQTY", "Equity Group Holdings Plc");
        kenyanStocks.put("COOP", "Co-operative Bank of Kenya Ltd");
        kenyanStocks.put("BAT", "British American Tobacco Kenya Plc");
        kenyanStocks.put("JUB", "Jubilee Holdings Ltd");
        kenyanStocks.put("ABSA", "ABSA Bank Kenya Plc");
    }
    
    /**
     * Get a list of available Kenyan stock symbols
     * @return list of stock symbols
     */
    public List<String> getKenyanStockSymbols() {
        return new ArrayList<>(kenyanStocks.keySet());
    }
    
    /**
     * Create a simulated stock quote for Kenyan stocks
     * In a production environment, this would fetch from an actual Kenyan market data API
     * 
     * @param symbol the Kenyan stock symbol
     * @return a simulated StockQuote
     */
    public StockQuote createKenyanStockQuote(String symbol) {
        // Check if symbol is a supported Kenyan stock
        if (!kenyanStocks.containsKey(symbol)) {
            log.error("Unsupported Kenyan stock symbol: {}", symbol);
            return null;
        }
        
        try {
            // In a real implementation, this would call an API for Kenyan markets
            // For now, we'll simulate with reasonable values
            String companyName = kenyanStocks.get(symbol);
            
            // Simulate a price based on the symbol's characters
            double basePrice = 0;
            for (char c : symbol.toCharArray()) {
                basePrice += c;
            }
            basePrice = basePrice % 500 + 50; // Keep price between 50 and 550 KES
            
            // Add some random variation to make it look realistic
            double randomFactor = 0.95 + Math.random() * 0.1; // Between 0.95 and 1.05
            BigDecimal price = BigDecimal.valueOf(basePrice * randomFactor);
            
            // Generate change amount (between -2% and +2%)
            double changePercent = -2.0 + Math.random() * 4.0;
            BigDecimal change = price.multiply(BigDecimal.valueOf(changePercent / 100));
            
            // Create volume (between 10,000 and 1,000,000 shares)
            long volume = 10000 + (long)(Math.random() * 990000);
            
            StockQuote quote = StockQuote.builder()
                .symbol(symbol)
                .companyName(companyName)
                .price(price)
                .change(change)
                .changePercent(BigDecimal.valueOf(changePercent))
                .volume(volume)
                .lastUpdated(LocalDateTime.now())
                .build();
            
            // Save to database
            return stockQuoteRepository.save(quote);
            
        } catch (Exception e) {
            log.error("Error creating Kenyan stock quote for {}: {}", symbol, e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets the most recent stock quote for a Kenyan stock
     * @param symbol the stock symbol
     * @return the most recent StockQuote or a new one if not found
     */
    public StockQuote getLatestKenyanQuote(String symbol) {
        if (!kenyanStocks.containsKey(symbol)) {
            log.error("Unsupported Kenyan stock symbol: {}", symbol);
            return null;
        }
        
        var quote = stockQuoteRepository.findTopBySymbolOrderByLastUpdatedDesc(symbol);
        
        if (quote.isPresent()) {
            return quote.get();
        } else {
            // If no quote exists, create a new one
            return createKenyanStockQuote(symbol);
        }
    }
    
    /**
     * Gets all Kenyan stock quotes
     * @return list of latest quotes for all Kenyan stocks
     */
    public List<StockQuote> getAllKenyanQuotes() {
        List<StockQuote> quotes = new ArrayList<>();
        
        for (String symbol : kenyanStocks.keySet()) {
            StockQuote quote = getLatestKenyanQuote(symbol);
            if (quote != null) {
                quotes.add(quote);
            }
        }
        
        return quotes;
    }
    
    /**
     * Scheduled task to update Kenyan stock quotes
     * This simulates real-time updates
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void updateKenyanStocks() {
        log.info("Updating Kenyan stock quotes");
        
        for (String symbol : kenyanStocks.keySet()) {
            try {
                createKenyanStockQuote(symbol);
                Thread.sleep(100); // Small delay
            } catch (Exception e) {
                log.error("Error updating Kenyan stock for {}: {}", symbol, e.getMessage());
            }
        }
        
        log.info("Kenyan stock quotes update completed");
    }
} 