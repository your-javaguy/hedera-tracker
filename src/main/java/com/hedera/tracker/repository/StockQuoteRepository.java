package com.hedera.tracker.repository;

import com.hedera.tracker.model.StockQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockQuoteRepository extends JpaRepository<StockQuote, Long> {
    
    Optional<StockQuote> findTopBySymbolOrderByLastUpdatedDesc(String symbol);
    
    List<StockQuote> findBySymbolOrderByLastUpdatedDesc(String symbol);
    
    List<StockQuote> findTop5ByOrderByVolumeDesc();
    
    List<StockQuote> findTop5ByOrderByChangePercentDesc();
    
    List<StockQuote> findTop5ByOrderByChangePercentAsc();
    
    List<StockQuote> findByLastUpdatedAfter(LocalDateTime time);
} 