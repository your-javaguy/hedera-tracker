package com.hedera.tracker.controller;

import com.hedera.tracker.model.TokenInfo;
import com.hedera.tracker.repository.TokenInfoRepository;
import com.hedera.tracker.service.TrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private TokenInfoRepository tokenInfoRepository;
    
    @Autowired
    private TrackerService trackerService;

    /**
     * Get all tokens
     */
    @GetMapping
    public List<TokenInfo> getAllTokens() {
        return tokenInfoRepository.findAll();
    }
    
    /**
     * Get top tokens by transaction count
     */
    @GetMapping("/top")
    public List<TokenInfo> getTopTokens() {
        return tokenInfoRepository.findTop10ByOrderByTransactionCountDesc();
    }
    
    /**
     * Get NFT tokens
     */
    @GetMapping("/nfts")
    public List<TokenInfo> getNftTokens() {
        return tokenInfoRepository.findByIsNft(true);
    }
    
    /**
     * Get fungible tokens
     */
    @GetMapping("/fungible")
    public List<TokenInfo> getFungibleTokens() {
        return tokenInfoRepository.findByIsNft(false);
    }
    
    /**
     * Get a specific token by ID
     */
    @GetMapping("/{tokenId}")
    public ResponseEntity<TokenInfo> getToken(@PathVariable String tokenId) {
        Optional<TokenInfo> tokenInfo = tokenInfoRepository.findById(tokenId);
        
        // If token is not in database, try to fetch it from the mirror node
        if (tokenInfo.isEmpty()) {
            TokenInfo fetchedToken = trackerService.fetchTokenInfo(tokenId);
            if (fetchedToken != null) {
                return ResponseEntity.ok(fetchedToken);
            }
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(tokenInfo.get());
    }
    
    /**
     * Add a token to track
     */
    @PostMapping
    public ResponseEntity<TokenInfo> addToken(@RequestParam String tokenId) {
        // Check if already exists
        Optional<TokenInfo> existingToken = tokenInfoRepository.findById(tokenId);
        if (existingToken.isPresent()) {
            return ResponseEntity.ok(existingToken.get());
        }
        
        // Fetch and save token
        TokenInfo tokenInfo = trackerService.fetchTokenInfo(tokenId);
        if (tokenInfo == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(tokenInfo);
    }
} 