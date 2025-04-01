package com.hedera.tracker.controller;

import com.hedera.tracker.model.StockQuote;
import com.hedera.tracker.service.KenyanStockService;
import com.hedera.tracker.service.StockMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    
    private final StockMarketService stockMarketService;
    private final KenyanStockService kenyanStockService;
    
    @Autowired
    public StockController(StockMarketService stockMarketService, KenyanStockService kenyanStockService) {
        this.stockMarketService = stockMarketService;
        this.kenyanStockService = kenyanStockService;
    }
    
    @GetMapping("/quotes")
    public ResponseEntity<List<StockQuote>> getAllQuotes() {
        return ResponseEntity.ok(stockMarketService.getAllLatestQuotes());
    }
    
    @GetMapping("/quotes/kenya")
    public ResponseEntity<List<StockQuote>> getAllKenyanQuotes() {
        return ResponseEntity.ok(kenyanStockService.getAllKenyanQuotes());
    }
    
    @GetMapping("/quotes/kenya/symbols")
    public ResponseEntity<List<String>> getKenyanSymbols() {
        return ResponseEntity.ok(kenyanStockService.getKenyanStockSymbols());
    }
    
    @GetMapping("/quote/{symbol}")
    public ResponseEntity<?> getQuote(@PathVariable String symbol) {
        // Check if it's a Kenyan stock
        if (kenyanStockService.getKenyanStockSymbols().contains(symbol)) {
            StockQuote quote = kenyanStockService.getLatestKenyanQuote(symbol);
            if (quote == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(quote);
        }
        
        // Otherwise treat as a regular stock
        StockQuote quote = stockMarketService.getLatestQuote(symbol);
        
        if (quote == null) {
            // Try to fetch from API
            quote = stockMarketService.fetchStockQuote(symbol);
            
            if (quote == null) {
                return ResponseEntity.notFound().build();
            }
        }
        
        return ResponseEntity.ok(quote);
    }
    
    @GetMapping("/refresh/{symbol}")
    public ResponseEntity<?> refreshQuote(@PathVariable String symbol) {
        // Check if it's a Kenyan stock
        if (kenyanStockService.getKenyanStockSymbols().contains(symbol)) {
            StockQuote quote = kenyanStockService.createKenyanStockQuote(symbol);
            if (quote == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(quote);
        }
        
        // Otherwise refresh from Alpha Vantage
        StockQuote quote = stockMarketService.fetchStockQuote(symbol);
        
        if (quote == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(quote);
    }
    
    @GetMapping("/market/summary")
    public ResponseEntity<Map<String, Object>> getMarketSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Add top gainers
        summary.put("gainers", stockMarketService.getTopGainers());
        
        // Add top losers
        summary.put("losers", stockMarketService.getTopLosers());
        
        // Add most active
        summary.put("mostActive", stockMarketService.getMostActiveStocks());
        
        // Add Kenyan market section
        summary.put("kenyanStocks", kenyanStockService.getAllKenyanQuotes());
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/market/kenya")
    public ResponseEntity<Map<String, Object>> getKenyanMarketSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Add all Kenyan stocks
        summary.put("stocks", kenyanStockService.getAllKenyanQuotes());
        
        return ResponseEntity.ok(summary);
    }
} 