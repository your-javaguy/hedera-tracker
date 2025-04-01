package com.hedera.tracker.service;

import com.hedera.tracker.model.Alert;
import com.hedera.tracker.model.TokenInfo;
import com.hedera.tracker.model.Transaction;
import com.hedera.tracker.repository.AlertRepository;
import com.hedera.tracker.repository.TokenInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private TokenInfoRepository tokenInfoRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Cache for token thresholds to avoid constant DB lookups
    private final Map<String, BigDecimal> tokenHighValueThresholds = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> tokenWhaleThresholds = new ConcurrentHashMap<>();
    
    /**
     * Processes a transaction to detect if it should generate alerts
     * @param transaction the transaction to analyze
     */
    public void processTransaction(Transaction transaction) {
        // Skip if no token ID
        if (transaction.getTokenId() == null || transaction.getTokenId().isEmpty()) {
            return;
        }
        
        boolean isHighValue = checkForHighValue(transaction);
        boolean isWhaleActivity = checkForWhaleActivity(transaction);
        
        // Set flags on transaction
        transaction.setIsHighValue(isHighValue);
        transaction.setIsWhaleActivity(isWhaleActivity);
        
        // Generate alerts if needed
        if (isHighValue) {
            createAndSendAlert(transaction, "HIGH_VALUE", "High-value transfer detected for token " 
                    + transaction.getTokenSymbol() + " (" + transaction.getTokenId() + ")");
        }
        
        if (isWhaleActivity) {
            createAndSendAlert(transaction, "WHALE_ACTIVITY", "Whale activity detected for token " 
                    + transaction.getTokenSymbol() + " (" + transaction.getTokenId() + ")");
        }
    }
    
    /**
     * Checks if a transaction represents a high-value transfer
     * @param transaction the transaction to check
     * @return true if high value, false otherwise
     */
    private boolean checkForHighValue(Transaction transaction) {
        BigDecimal threshold = getHighValueThreshold(transaction.getTokenId());
        return transaction.getAmount().compareTo(threshold) > 0;
    }
    
    /**
     * Checks if a transaction represents whale activity
     * @param transaction the transaction to check
     * @return true if whale activity, false otherwise
     */
    private boolean checkForWhaleActivity(Transaction transaction) {
        BigDecimal threshold = getWhaleThreshold(transaction.getTokenId());
        return transaction.getAmount().compareTo(threshold) > 0;
    }
    
    /**
     * Creates and sends an alert based on the transaction
     * @param transaction the transaction that triggered the alert
     * @param alertType the type of alert (HIGH_VALUE or WHALE_ACTIVITY)
     * @param description description of the alert
     */
    private void createAndSendAlert(Transaction transaction, String alertType, String description) {
        Alert alert = Alert.builder()
                .transactionId(transaction.getTransactionId())
                .alertType(alertType)
                .description(description)
                .tokenId(transaction.getTokenId())
                .tokenSymbol(transaction.getTokenSymbol())
                .acknowledged(false)
                .createdAt(Instant.now())
                .build();
        
        // Save to database
        alertRepository.save(alert);
        
        // Send via WebSocket
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        
        log.info("Created alert: {} - {}", alertType, description);
    }
    
    /**
     * Gets the high-value threshold for a token
     * @param tokenId the token ID
     * @return the threshold value
     */
    private BigDecimal getHighValueThreshold(String tokenId) {
        // Check cache first
        if (tokenHighValueThresholds.containsKey(tokenId)) {
            return tokenHighValueThresholds.get(tokenId);
        }
        
        // Calculate threshold based on token info
        Optional<TokenInfo> tokenInfoOpt = tokenInfoRepository.findById(tokenId);
        BigDecimal threshold;
        
        if (tokenInfoOpt.isPresent()) {
            TokenInfo tokenInfo = tokenInfoOpt.get();
            // For NFTs, any transfer is high value
            if (tokenInfo.getIsNft()) {
                threshold = BigDecimal.ONE;
            } else {
                // For fungible tokens, base it on market cap or a default
                if (tokenInfo.getMarketCap() != null && tokenInfo.getMarketCap().compareTo(BigDecimal.ZERO) > 0) {
                    // 0.1% of market cap is considered high value
                    threshold = tokenInfo.getMarketCap().multiply(new BigDecimal("0.001"));
                } else {
                    // Default threshold
                    threshold = new BigDecimal("1000");
                }
            }
        } else {
            // Default threshold if token not found
            threshold = new BigDecimal("1000");
        }
        
        // Cache the result
        tokenHighValueThresholds.put(tokenId, threshold);
        return threshold;
    }
    
    /**
     * Gets the whale activity threshold for a token
     * @param tokenId the token ID
     * @return the threshold value
     */
    private BigDecimal getWhaleThreshold(String tokenId) {
        // Check cache first
        if (tokenWhaleThresholds.containsKey(tokenId)) {
            return tokenWhaleThresholds.get(tokenId);
        }
        
        // Calculate threshold based on token info
        Optional<TokenInfo> tokenInfoOpt = tokenInfoRepository.findById(tokenId);
        BigDecimal threshold;
        
        if (tokenInfoOpt.isPresent()) {
            TokenInfo tokenInfo = tokenInfoOpt.get();
            // For NFTs, consider > 5 transfers as whale activity
            if (tokenInfo.getIsNft()) {
                threshold = new BigDecimal("5");
            } else {
                // For fungible tokens, base it on market cap or a default
                if (tokenInfo.getMarketCap() != null && tokenInfo.getMarketCap().compareTo(BigDecimal.ZERO) > 0) {
                    // 1% of market cap is considered whale activity
                    threshold = tokenInfo.getMarketCap().multiply(new BigDecimal("0.01"));
                } else {
                    // Default threshold
                    threshold = new BigDecimal("10000");
                }
            }
        } else {
            // Default threshold if token not found
            threshold = new BigDecimal("10000");
        }
        
        // Cache the result
        tokenWhaleThresholds.put(tokenId, threshold);
        return threshold;
    }
    
    /**
     * Clears the threshold caches
     */
    public void clearThresholdCaches() {
        tokenHighValueThresholds.clear();
        tokenWhaleThresholds.clear();
    }
    
    /**
     * Sets a custom high-value threshold for a token
     * @param tokenId the token ID
     * @param threshold the new threshold
     */
    public void setHighValueThreshold(String tokenId, BigDecimal threshold) {
        tokenHighValueThresholds.put(tokenId, threshold);
    }
    
    /**
     * Sets a custom whale threshold for a token
     * @param tokenId the token ID
     * @param threshold the new threshold
     */
    public void setWhaleThreshold(String tokenId, BigDecimal threshold) {
        tokenWhaleThresholds.put(tokenId, threshold);
    }
} 