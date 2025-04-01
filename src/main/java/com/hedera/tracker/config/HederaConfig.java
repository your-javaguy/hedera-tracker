package com.hedera.tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Scope;

@Configuration
public class HederaConfig {

    @Value("${hedera.mirror.mainnet.api.baseUrl:https://mainnet-public.mirrornode.hedera.com/api/v1}")
    private String mainnetMirrorNodeBaseUrl;
    
    @Value("${hedera.mirror.testnet.api.baseUrl:https://testnet.mirrornode.hedera.com/api/v1}")
    private String testnetMirrorNodeBaseUrl;
    
    @Value("${hedera.network:MAINNET}")
    private String activeNetwork;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public String mirrorNodeBaseUrl() {
        return isTestnet() ? testnetMirrorNodeBaseUrl : mainnetMirrorNodeBaseUrl;
    }
    
    @Bean
    public boolean isTestnet() {
        return "TESTNET".equalsIgnoreCase(activeNetwork);
    }
    
    public void setActiveNetwork(String network) {
        this.activeNetwork = network;
    }
    
    @Bean
    public String activeNetwork() {
        return activeNetwork;
    }
} 