package com.hedera.tracker.repository;

import com.hedera.tracker.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    List<Alert> findByAcknowledged(Boolean acknowledged);
    
    List<Alert> findByAlertType(String alertType);
    
    List<Alert> findByTokenId(String tokenId);
    
    List<Alert> findByCreatedAtBetween(Instant startTime, Instant endTime);
    
    List<Alert> findTop50ByOrderByCreatedAtDesc();

    int countByAcknowledgedFalse();
} 