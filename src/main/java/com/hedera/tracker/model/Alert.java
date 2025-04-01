package com.hedera.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Entity to store alert information for high-value transfers or whale activity
 */
@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String transactionId;
    private String alertType; // HIGH_VALUE, WHALE_ACTIVITY, etc.
    private String description;
    private String tokenId;
    private String tokenSymbol;
    private Boolean acknowledged;
    private Instant createdAt;
} 