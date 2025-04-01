package com.hedera.tracker.config;

import com.hedera.tracker.model.Alert;
import com.hedera.tracker.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Initializes sample alert data
 */
@Component
@Order(3) // Run after other initializers
public class AlertDataInitializer implements CommandLineRunner {

    @Autowired
    private AlertRepository alertRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking for alert data...");
        
        if (alertRepository.count() == 0) {
            System.out.println("No alerts found, creating sample data...");
            
            // Create sample high-value transaction alert
            Alert highValueAlert = Alert.builder()
                    .transactionId("0.0.123456@1622222222.222222222")
                    .alertType("HIGH_VALUE")
                    .description("High value transfer of 100,000 HBAR detected")
                    .tokenId("0.0.1")
                    .tokenSymbol("HBAR")
                    .acknowledged(false)
                    .createdAt(Instant.now().minusSeconds(3600)) // 1 hour ago
                    .build();
            alertRepository.save(highValueAlert);
            
            // Create sample whale activity alert
            Alert whaleAlert = Alert.builder()
                    .transactionId("0.0.789012@1622222222.222222222")
                    .alertType("WHALE_ACTIVITY")
                    .description("Whale activity: 5% of token supply transferred")
                    .tokenId("0.0.123456")
                    .tokenSymbol("TEST")
                    .acknowledged(false)
                    .createdAt(Instant.now().minusSeconds(1800)) // 30 minutes ago
                    .build();
            alertRepository.save(whaleAlert);
            
            // Create sample acknowledged alert
            Alert acknowledgedAlert = Alert.builder()
                    .transactionId("0.0.345678@1622222222.222222222")
                    .alertType("HIGH_VALUE")
                    .description("High value transfer of 50,000 HBAR detected")
                    .tokenId("0.0.1")
                    .tokenSymbol("HBAR")
                    .acknowledged(true)
                    .createdAt(Instant.now().minusSeconds(7200)) // 2 hours ago
                    .build();
            alertRepository.save(acknowledgedAlert);
            
            System.out.println("Created " + alertRepository.count() + " sample alerts");
        } else {
            System.out.println("Alert data already exists. Count: " + alertRepository.count());
        }
    }
} 