package com.hedera.tracker.repository;

import com.hedera.tracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    List<Transaction> findByTokenId(String tokenId);
    
    List<Transaction> findByTokenIdAndTimestampBetween(String tokenId, Instant startTime, Instant endTime);
    
    List<Transaction> findByIsHighValue(Boolean isHighValue);
    
    List<Transaction> findByIsWhaleActivity(Boolean isWhaleActivity);
    
    List<Transaction> findTop50ByOrderByTimestampDesc();
    
    @Query("SELECT t FROM Transaction t WHERE t.amount > (SELECT AVG(t2.amount) * 2 FROM Transaction t2 WHERE t2.tokenId = t.tokenId)")
    List<Transaction> findUnusualTransactions();
} 