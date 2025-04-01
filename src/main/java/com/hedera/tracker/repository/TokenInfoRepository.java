package com.hedera.tracker.repository;

import com.hedera.tracker.model.TokenInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenInfoRepository extends JpaRepository<TokenInfo, String> {
    
    List<TokenInfo> findByIsNft(Boolean isNft);
    
    List<TokenInfo> findTop10ByOrderByTransactionCountDesc();
    
    List<TokenInfo> findTop10ByOrderByMarketCapDesc();
    
    List<TokenInfo> findTop10ByOrderByPriceChangePercentDesc();
} 