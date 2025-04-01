package com.hedera.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Hedera Asset Tracker
 */
@SpringBootApplication
@EnableScheduling
public class HederaTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HederaTrackerApplication.class, args);
    }
} 