package com.hedera.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity to store transaction information
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    private String transactionId;
    
    private String type;
    private String tokenId;
    private String tokenSymbol;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private BigDecimal fee;
    private Instant timestamp;
    private String status;
    private Boolean isHighValue;
    private Boolean isWhaleActivity;
} 