package com.hedera.tracker.controller;

import com.hedera.tracker.repository.AlertRepository;
import com.hedera.tracker.repository.TokenInfoRepository;
import com.hedera.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @Autowired
    private TokenInfoRepository tokenInfoRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AlertRepository alertRepository;

    /**
     * Home page - redirects to dashboard
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * Main dashboard page
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("recentTransactions", transactionRepository.findTop50ByOrderByTimestampDesc());
        model.addAttribute("topTokens", tokenInfoRepository.findTop10ByOrderByTransactionCountDesc());
        model.addAttribute("recentAlerts", alertRepository.findTop50ByOrderByCreatedAtDesc());
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.findByAcknowledged(false).size());
        return "dashboard";
    }
    
    /**
     * Token details page
     */
    @GetMapping("/tokens/{tokenId}")
    public String tokenDetails(@PathVariable String tokenId, Model model) {
        model.addAttribute("token", tokenInfoRepository.findById(tokenId).orElse(null));
        model.addAttribute("transactions", transactionRepository.findByTokenId(tokenId));
        model.addAttribute("alerts", alertRepository.findByTokenId(tokenId));
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.findByAcknowledged(false).size());
        return "token-details";
    }
    
    /**
     * Alerts page
     */
    @GetMapping("/alerts")
    public String alerts(Model model) {
        model.addAttribute("alerts", alertRepository.findTop50ByOrderByCreatedAtDesc());
        model.addAttribute("unacknowledgedAlerts", alertRepository.findByAcknowledged(false));
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.findByAcknowledged(false).size());
        return "alerts";
    }
    
    /**
     * Transactions page
     */
    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("recentTransactions", transactionRepository.findTop50ByOrderByTimestampDesc());
        model.addAttribute("highValueTransactions", transactionRepository.findByIsHighValue(true));
        model.addAttribute("whaleActivityTransactions", transactionRepository.findByIsWhaleActivity(true));
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.findByAcknowledged(false).size());
        return "transactions";
    }
    
    /**
     * Tokens list page
     */
    @GetMapping("/tokens")
    public String tokens(Model model) {
        model.addAttribute("tokens", tokenInfoRepository.findAll());
        model.addAttribute("nftTokens", tokenInfoRepository.findByIsNft(true));
        model.addAttribute("fungibleTokens", tokenInfoRepository.findByIsNft(false));
        model.addAttribute("unacknowledgedAlertsCount", alertRepository.findByAcknowledged(false).size());
        return "tokens";
    }
} 