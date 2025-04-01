package com.hedera.tracker.controller;

import com.hedera.tracker.model.Transaction;
import com.hedera.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Get all recent transactions
     */
    @GetMapping("/recent")
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop50ByOrderByTimestampDesc();
    }
    
    /**
     * Get transactions for a specific token
     */
    @GetMapping("/token/{tokenId}")
    public List<Transaction> getTransactionsByToken(@PathVariable String tokenId) {
        return transactionRepository.findByTokenId(tokenId);
    }
    
    /**
     * Get transactions by time range
     */
    @GetMapping("/token/{tokenId}/timerange")
    public List<Transaction> getTransactionsByTimeRange(
            @PathVariable String tokenId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        return transactionRepository.findByTokenIdAndTimestampBetween(tokenId, startTime, endTime);
    }
    
    /**
     * Get high-value transactions
     */
    @GetMapping("/high-value")
    public List<Transaction> getHighValueTransactions() {
        return transactionRepository.findByIsHighValue(true);
    }
    
    /**
     * Get whale activity transactions
     */
    @GetMapping("/whale-activity")
    public List<Transaction> getWhaleActivityTransactions() {
        return transactionRepository.findByIsWhaleActivity(true);
    }
    
    /**
     * Get unusual transactions
     */
    @GetMapping("/unusual")
    public List<Transaction> getUnusualTransactions() {
        return transactionRepository.findUnusualTransactions();
    }
    
    /**
     * Get a specific transaction by ID
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionId) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        return transaction.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 