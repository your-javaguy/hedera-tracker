package com.hedera.tracker.controller;

import com.hedera.tracker.config.HederaConfig;
import com.hedera.tracker.model.StockQuote;
import com.hedera.tracker.repository.AlertRepository;
import com.hedera.tracker.service.KenyanStockService;
import com.hedera.tracker.service.StockMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

/**
 * Controller for authentication and profile-related views
 */
@Controller
public class ViewController {
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private StockMarketService stockMarketService;
    
    @Autowired
    private KenyanStockService kenyanStockService;
    
    @Autowired
    private HederaConfig hederaConfig;

    @GetMapping("/secure-dashboard")
    public String securedDashboard(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "dashboard";
    }

    /**
     * Display profile page
     * @return the profile view
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "profile";
    }
    
    /**
     * Display settings page
     * @return the settings view
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @GetMapping("/settings")
    public String settings(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "settings";
    }
    
    /**
     * Display analytics page
     * @return the analytics view
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @GetMapping("/analytics")
    public String analytics(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "analytics";
    }
    
    @GetMapping("/secure-alerts")
    public String securedAlerts(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "alerts";
    }
    
    @GetMapping("/secure-tokens")
    public String securedTokens(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "tokens";
    }
    
    @GetMapping("/secure-transactions")
    public String securedTransactions(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "transactions";
    }
    
    @GetMapping("/token/{id}")
    public String tokenDetails(@PathVariable String id, Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("tokenId", id);
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        return "token-details";
    }
    
    @GetMapping("/stocks")
    public String stocks(Model model, Principal principal) {
        List<StockQuote> quotes = stockMarketService.getAllLatestQuotes();
        model.addAttribute("quotes", quotes);
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        
        // Add market summary data
        model.addAttribute("gainers", stockMarketService.getTopGainers());
        model.addAttribute("losers", stockMarketService.getTopLosers());
        model.addAttribute("mostActive", stockMarketService.getMostActiveStocks());
        
        // Add Kenyan stocks data
        model.addAttribute("kenyanStocks", kenyanStockService.getAllKenyanQuotes());
        
        return "stocks";
    }
    
    @GetMapping("/stock/{symbol}")
    public String stockDetails(@PathVariable String symbol, Model model, Principal principal) {
        StockQuote quote;
        boolean isKenyanStock = kenyanStockService.getKenyanStockSymbols().contains(symbol);
        
        if (isKenyanStock) {
            quote = kenyanStockService.getLatestKenyanQuote(symbol);
        } else {
            quote = stockMarketService.getLatestQuote(symbol);
            if (quote == null) {
                quote = stockMarketService.fetchStockQuote(symbol);
            }
        }
        
        model.addAttribute("quote", quote);
        model.addAttribute("symbol", symbol);
        model.addAttribute("isKenyanStock", isKenyanStock);
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        
        return "stock-details";
    }
    
    @GetMapping("/stocks/kenya")
    public String kenyanStocks(Model model, Principal principal) {
        List<StockQuote> quotes = kenyanStockService.getAllKenyanQuotes();
        model.addAttribute("quotes", quotes);
        model.addAttribute("username", principal.getName());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.countByAcknowledgedFalse());
        model.addAttribute("isTestnet", hederaConfig.isTestnet());
        model.addAttribute("activeNetwork", hederaConfig.activeNetwork());
        model.addAttribute("kenyanSymbols", kenyanStockService.getKenyanStockSymbols());
        
        return "kenyan-stocks";
    }
} 