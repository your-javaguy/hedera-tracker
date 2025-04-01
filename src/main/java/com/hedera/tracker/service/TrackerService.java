package com.hedera.tracker.service;

import com.hedera.tracker.model.TokenInfo;
import com.hedera.tracker.model.Transaction;
import com.hedera.tracker.repository.TokenInfoRepository;
import com.hedera.tracker.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrackerService {

    @Autowired
    private HederaMirrorService mirrorService;
    
    @Autowired
    private TokenInfoRepository tokenInfoRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Value("${hedera.refresh.interval.seconds:30}")
    private int refreshInterval;
    
    private final int TRANSACTION_FETCH_LIMIT = 50;
    private final int TOKEN_FETCH_LIMIT = 20;
    
    /**
     * Scheduled method to fetch recent transactions from the Hedera network
     */
    @Scheduled(fixedDelayString = "${hedera.refresh.interval.seconds:30}000")
    public void fetchRecentTransactions() {
        log.debug("Fetching recent transactions...");
        try {
            List<Transaction> transactions = mirrorService.fetchRecentTransactions(TRANSACTION_FETCH_LIMIT);
            
            for (Transaction transaction : transactions) {
                // Skip transactions without token ID
                if (transaction.getTokenId() == null || transaction.getTokenId().isEmpty()) {
                    continue;
                }
                
                // Check if transaction already exists
                Optional<Transaction> existingTransaction = transactionRepository.findById(transaction.getTransactionId());
                if (existingTransaction.isPresent()) {
                    continue;
                }
                
                // Get token info
                Optional<TokenInfo> tokenInfoOpt = tokenInfoRepository.findById(transaction.getTokenId());
                if (tokenInfoOpt.isPresent()) {
                    TokenInfo tokenInfo = tokenInfoOpt.get();
                    transaction.setTokenSymbol(tokenInfo.getSymbol());
                    
                    // Update transaction count
                    tokenInfo.setTransactionCount(tokenInfo.getTransactionCount() + 1);
                    tokenInfo.setLastUpdated(Instant.now());
                    tokenInfoRepository.save(tokenInfo);
                } else {
                    // Fetch token info if not available
                    TokenInfo newTokenInfo = mirrorService.fetchTokenInfo(transaction.getTokenId());
                    if (newTokenInfo != null) {
                        newTokenInfo.setTransactionCount(1L);
                        tokenInfoRepository.save(newTokenInfo);
                        transaction.setTokenSymbol(newTokenInfo.getSymbol());
                    }
                }
                
                // Process transaction for alerts
                alertService.processTransaction(transaction);
                
                // Save transaction
                transactionRepository.save(transaction);
                
                // Send to WebSocket clients
                messagingTemplate.convertAndSend("/topic/transactions", transaction);
            }
            
            // Send updated list to clients
            List<Transaction> recentTransactions = transactionRepository.findTop50ByOrderByTimestampDesc();
            messagingTemplate.convertAndSend("/topic/transactions/recent", recentTransactions);
            
        } catch (Exception e) {
            log.error("Error fetching recent transactions: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Scheduled method to update token information
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void updateTokenInfo() {
        log.debug("Updating token information...");
        try {
            // Fetch popular tokens
            List<TokenInfo> popularTokens = mirrorService.fetchPopularTokens(TOKEN_FETCH_LIMIT);
            
            for (TokenInfo tokenInfo : popularTokens) {
                Optional<TokenInfo> existingTokenOpt = tokenInfoRepository.findById(tokenInfo.getTokenId());
                
                if (existingTokenOpt.isPresent()) {
                    TokenInfo existingToken = existingTokenOpt.get();
                    
                    // Update fields but preserve transaction count
                    tokenInfo.setTransactionCount(existingToken.getTransactionCount());
                    tokenInfo.setMarketCap(existingToken.getMarketCap());
                    tokenInfo.setCurrentPrice(existingToken.getCurrentPrice());
                    tokenInfo.setPriceChangePercent(existingToken.getPriceChangePercent());
                }
                
                // Update last updated timestamp
                tokenInfo.setLastUpdated(Instant.now());
                
                // Save to database
                tokenInfoRepository.save(tokenInfo);
            }
            
            // Send to WebSocket clients
            List<TokenInfo> tokens = tokenInfoRepository.findAll();
            messagingTemplate.convertAndSend("/topic/tokens", tokens);
            
        } catch (Exception e) {
            log.error("Error updating token information: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manually fetch transactions for a specific token
     * @param tokenId token ID to fetch transactions for
     * @return list of transactions found
     */
    public List<Transaction> fetchTransactionsForToken(String tokenId) {
        try {
            List<Transaction> transactions = mirrorService.fetchTokenTransactions(tokenId, TRANSACTION_FETCH_LIMIT);
            List<Transaction> savedTransactions = new ArrayList<>();
            
            for (Transaction transaction : transactions) {
                // Check if transaction already exists
                Optional<Transaction> existingTransaction = transactionRepository.findById(transaction.getTransactionId());
                if (existingTransaction.isPresent()) {
                    savedTransactions.add(existingTransaction.get());
                    continue;
                }
                
                // Get token info for symbol
                Optional<TokenInfo> tokenInfoOpt = tokenInfoRepository.findById(tokenId);
                tokenInfoOpt.ifPresent(tokenInfo -> transaction.setTokenSymbol(tokenInfo.getSymbol()));
                
                // Process transaction for alerts
                alertService.processTransaction(transaction);
                
                // Save transaction
                transactionRepository.save(transaction);
                savedTransactions.add(transaction);
            }
            
            return savedTransactions;
            
        } catch (Exception e) {
            log.error("Error fetching transactions for token {}: {}", tokenId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetch token info for a specific token ID
     * @param tokenId token ID to fetch
     * @return token info object
     */
    public TokenInfo fetchTokenInfo(String tokenId) {
        // Check if already exists
        Optional<TokenInfo> existingTokenOpt = tokenInfoRepository.findById(tokenId);
        if (existingTokenOpt.isPresent()) {
            return existingTokenOpt.get();
        }
        
        // Fetch from API
        TokenInfo tokenInfo = mirrorService.fetchTokenInfo(tokenId);
        if (tokenInfo != null) {
            tokenInfo.setTransactionCount(0L);
            tokenInfoRepository.save(tokenInfo);
        }
        
        return tokenInfo;
    }
} 