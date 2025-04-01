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
 * Entity to store token information
 */
@Entity
@Table(name = "token_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    
    @Id
    private String tokenId;
    
    private String name;
    private String symbol;
    private Boolean isNft;
    private Long totalSupply;
    private Integer decimals;
    private BigDecimal marketCap;
    private BigDecimal currentPrice;
    private BigDecimal priceChangePercent;
    private Long transactionCount;
    private Instant lastUpdated;
} 