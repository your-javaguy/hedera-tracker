package com.hedera.tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;

@Configuration
public class StockApiConfig {

    @Value("${stock.api.key:}")
    private String stockApiKey;
    
    @Value("${stock.api.host:alphavantage.co}")
    private String stockApiHost;
    
    @Value("${stock.api.base-url:https://alphavantage.co/query}")
    private String stockApiBaseUrl;
    
    @Value("${stock.symbols.default:AAPL,MSFT,GOOGL,AMZN,TSLA}")
    private String defaultStockSymbols;
    
    @Bean(name = "stockApiRestTemplate")
    public RestTemplate stockApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add interceptor to set API key header
        if (stockApiKey != null && !stockApiKey.isEmpty()) {
            restTemplate.setInterceptors(
                Collections.singletonList((ClientHttpRequestInterceptor) (request, body, execution) -> {
                    HttpHeaders headers = request.getHeaders();
                    headers.add("X-API-KEY", stockApiKey);
                    return execution.execute(request, body);
                }));
        }
        
        return restTemplate;
    }
    
    @Bean
    public String stockApiBaseUrl() {
        return stockApiBaseUrl;
    }
    
    @Bean
    public String stockApiKey() {
        return stockApiKey;
    }
    
    @Bean
    public String stockApiHost() {
        return stockApiHost;
    }
    
    @Bean
    public List<String> defaultStockSymbols() {
        return Arrays.asList(defaultStockSymbols.split(","));
    }

     @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow all origins, headers, and methods for development
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 