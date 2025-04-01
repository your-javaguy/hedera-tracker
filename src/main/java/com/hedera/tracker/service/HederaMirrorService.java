package com.hedera.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.tracker.model.TokenInfo;
import com.hedera.tracker.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HederaMirrorService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private String mirrorNodeBaseUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Fetches token information from the Mirror Node API
     * @param tokenId the token ID
     * @return TokenInfo object with token details
     */
    public TokenInfo fetchTokenInfo(String tokenId) {
        try {
            String url = mirrorNodeBaseUrl + "/tokens/" + tokenId;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            TokenInfo tokenInfo = TokenInfo.builder()
                    .tokenId(tokenId)
                    .name(root.path("name").asText())
                    .symbol(root.path("symbol").asText())
                    .isNft(root.path("type").asText().equals("NON_FUNGIBLE_UNIQUE"))
                    .totalSupply(root.path("total_supply").asLong())
                    .decimals(root.path("decimals").asInt())
                    .lastUpdated(Instant.now())
                    .transactionCount(0L)
                    .build();
            
            return tokenInfo;
        } catch (Exception e) {
            log.error("Error fetching token info for token ID {}: {}", tokenId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Fetches recent transactions for a token from the Mirror Node API
     * @param tokenId the token ID
     * @param limit maximum number of transactions to fetch
     * @return list of recent transactions
     */
    public List<Transaction> fetchTokenTransactions(String tokenId, int limit) {
        try {
            String url = mirrorNodeBaseUrl + "/transactions?token.id=" + tokenId + "&limit=" + limit + "&order=desc";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode transactions = root.path("transactions");
            
            List<Transaction> result = new ArrayList<>();
            
            for (JsonNode txNode : transactions) {
                String transactionId = txNode.path("transaction_id").asText();
                
                Transaction tx = Transaction.builder()
                        .transactionId(transactionId)
                        .type(txNode.path("name").asText())
                        .tokenId(tokenId)
                        .fromAccount(getAccountFromTransaction(txNode, "from"))
                        .toAccount(getAccountFromTransaction(txNode, "to"))
                        .amount(new BigDecimal(txNode.path("amount").asText("0")))
                        .fee(new BigDecimal(txNode.path("charged_tx_fee").asText("0")))
                        .timestamp(Instant.ofEpochSecond(txNode.path("consensus_timestamp").asLong()))
                        .status(txNode.path("result").asText())
                        .isHighValue(false)
                        .isWhaleActivity(false)
                        .build();
                
                result.add(tx);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error fetching transactions for token ID {}: {}", tokenId, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetches recent transactions from the Mirror Node API
     * @param limit maximum number of transactions to fetch
     * @return list of recent transactions
     */
    public List<Transaction> fetchRecentTransactions(int limit) {
        try {
            String url = mirrorNodeBaseUrl + "/transactions?limit=" + limit + "&order=desc";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode transactions = root.path("transactions");
            
            List<Transaction> result = new ArrayList<>();
            
            for (JsonNode txNode : transactions) {
                String transactionId = txNode.path("transaction_id").asText();
                
                Transaction tx = Transaction.builder()
                        .transactionId(transactionId)
                        .type(txNode.path("name").asText())
                        .tokenId(getTokenFromTransaction(txNode))
                        .fromAccount(getAccountFromTransaction(txNode, "from"))
                        .toAccount(getAccountFromTransaction(txNode, "to"))
                        .amount(new BigDecimal(txNode.path("amount").asText("0")))
                        .fee(new BigDecimal(txNode.path("charged_tx_fee").asText("0")))
                        .timestamp(Instant.ofEpochSecond(txNode.path("consensus_timestamp").asLong()))
                        .status(txNode.path("result").asText())
                        .isHighValue(false)
                        .isWhaleActivity(false)
                        .build();
                
                result.add(tx);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error fetching recent transactions: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Helper method to extract account addresses from transaction data
     */
    private String getAccountFromTransaction(JsonNode txNode, String direction) {
        JsonNode transfers = txNode.path("transfers");
        if (transfers.isArray() && !transfers.isEmpty()) {
            for (JsonNode transfer : transfers) {
                if (direction.equals("from") && transfer.path("amount").asLong() < 0) {
                    return transfer.path("account").asText();
                } else if (direction.equals("to") && transfer.path("amount").asLong() > 0) {
                    return transfer.path("account").asText();
                }
            }
        }
        return "";
    }
    
    /**
     * Helper method to extract token ID from transaction data
     */
    private String getTokenFromTransaction(JsonNode txNode) {
        JsonNode tokenTransfers = txNode.path("token_transfers");
        if (tokenTransfers.isArray() && !tokenTransfers.isEmpty()) {
            return tokenTransfers.get(0).path("token_id").asText();
        }
        return "";
    }
    
    /**
     * Fetches popular tokens from the Mirror Node API
     * @param limit maximum number of tokens to fetch
     * @return list of token information
     */
    public List<TokenInfo> fetchPopularTokens(int limit) {
        try {
            String url = mirrorNodeBaseUrl + "/tokens?limit=" + limit + "&order=desc";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode tokens = root.path("tokens");
            
            List<TokenInfo> result = new ArrayList<>();
            
            for (JsonNode tokenNode : tokens) {
                String tokenId = tokenNode.path("token_id").asText();
                
                TokenInfo tokenInfo = TokenInfo.builder()
                        .tokenId(tokenId)
                        .name(tokenNode.path("name").asText())
                        .symbol(tokenNode.path("symbol").asText())
                        .isNft(tokenNode.path("type").asText().equals("NON_FUNGIBLE_UNIQUE"))
                        .totalSupply(tokenNode.path("total_supply").asLong())
                        .decimals(tokenNode.path("decimals").asInt())
                        .lastUpdated(Instant.now())
                        .transactionCount(0L)
                        .build();
                
                result.add(tokenInfo);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error fetching popular tokens: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
} 