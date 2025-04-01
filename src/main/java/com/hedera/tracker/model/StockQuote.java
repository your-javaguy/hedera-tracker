package com.hedera.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_quotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal change;
    
    @Column(name = "change_percent", precision = 19, scale = 4)
    private BigDecimal changePercent;
    
    @Column(name = "market_cap", precision = 19, scale = 2)
    private BigDecimal marketCap;
    
    private Long volume;
    
    @Column(name = "day_high", precision = 19, scale = 4)
    private BigDecimal dayHigh;
    
    @Column(name = "day_low", precision = 19, scale = 4)
    private BigDecimal dayLow;
    
    @Column(name = "year_high", precision = 19, scale = 4)
    private BigDecimal yearHigh;
    
    @Column(name = "year_low", precision = 19, scale = 4)
    private BigDecimal yearLow;
    
    @Column(name = "pe_ratio", precision = 19, scale = 4)
    private BigDecimal peRatio;
    
    @Column(name = "dividend_yield", precision = 19, scale = 4)
    private BigDecimal dividendYield;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
} 