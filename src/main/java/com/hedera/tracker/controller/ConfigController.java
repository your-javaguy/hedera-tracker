package com.hedera.tracker.controller;

import com.hedera.tracker.config.HederaConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    private final HederaConfig hederaConfig;
    
    @Autowired
    public ConfigController(HederaConfig hederaConfig) {
        this.hederaConfig = hederaConfig;
    }
    
    @GetMapping("/network")
    public ResponseEntity<Map<String, String>> getNetwork() {
        return ResponseEntity.ok(Collections.singletonMap("network", hederaConfig.activeNetwork()));
    }
    
    @PostMapping("/network")
    public ResponseEntity<Map<String, String>> setNetwork(@RequestBody NetworkConfig config) {
        String network = config.getNetwork().toUpperCase();
        
        // Only allow MAINNET or TESTNET
        if (!network.equals("MAINNET") && !network.equals("TESTNET")) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "Invalid network. Must be MAINNET or TESTNET")
            );
        }
        
        // Update the active network
        hederaConfig.setActiveNetwork(network);
        
        return ResponseEntity.ok(Collections.singletonMap("network", network));
    }
    
    @Data
    public static class NetworkConfig {
        private String network;
    }
} 