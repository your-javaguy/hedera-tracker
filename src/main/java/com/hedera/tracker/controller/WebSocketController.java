package com.hedera.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller handling WebSocket messages
 */
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle request for transaction data
     */
    @MessageMapping("/transactions")
    public void handleTransactionsRequest() {
        // This would typically fetch data from a service and send it back
        // For now, we'll just send a sample transaction for demonstration
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("id", "0.0.1234");
        transaction.put("type", "Transfer");
        transaction.put("from", "0x12ab...34cd");
        transaction.put("to", "0x78ef...90gh");
        transaction.put("amount", "1,250 HBAR");
        transaction.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/transactions", transaction);
    }

    /**
     * Handle request for alert data
     */
    @MessageMapping("/alerts")
    public void handleAlertsRequest() {
        // This would typically fetch data from a service and send it back
        // For now, we'll just send a sample alert for demonstration
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", "1");
        alert.put("severity", "Warning");
        alert.put("title", "Unusual Trading Activity");
        alert.put("description", "Detected abnormal trading volume for token HBAR");
        alert.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/alerts", alert);
    }
    
    /**
     * Handle request for token data
     */
    @MessageMapping("/tokens")
    public void handleTokensRequest() {
        // This would typically fetch data from a service and send it back
        // For now, we'll just send a sample token list for demonstration
        Map<String, Object> token = new HashMap<>();
        token.put("id", "0.0.5678");
        token.put("name", "Hedera");
        token.put("symbol", "HBAR");
        token.put("type", "Fungible");
        token.put("supply", "50,000,000,000");
        token.put("transactions", 12345);
        
        messagingTemplate.convertAndSend("/topic/tokens", Collections.singletonList(token));
    }
} 