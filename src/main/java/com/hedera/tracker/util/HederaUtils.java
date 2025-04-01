package com.hedera.tracker.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Utility class for Hedera-related functions
 */
@Slf4j
public class HederaUtils {

    /**
     * Converts a token amount from raw integer value to decimal value based on token decimals
     * @param amount raw token amount
     * @param decimals token decimals
     * @return formatted amount
     */
    public static BigDecimal convertToDecimal(BigInteger amount, int decimals) {
        try {
            BigDecimal decimalValue = new BigDecimal(amount);
            BigDecimal divisor = BigDecimal.TEN.pow(decimals);
            return decimalValue.divide(divisor, decimals, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error converting token amount: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Converts a token amount from raw long value to decimal value based on token decimals
     * @param amount raw token amount
     * @param decimals token decimals
     * @return formatted amount
     */
    public static BigDecimal convertToDecimal(long amount, int decimals) {
        return convertToDecimal(BigInteger.valueOf(amount), decimals);
    }
    
    /**
     * Converts a token amount from decimal to raw integer value based on token decimals
     * @param amount decimal amount
     * @param decimals token decimals
     * @return raw integer amount
     */
    public static BigInteger convertToRaw(BigDecimal amount, int decimals) {
        try {
            BigDecimal multiplier = BigDecimal.TEN.pow(decimals);
            return amount.multiply(multiplier).toBigInteger();
        } catch (Exception e) {
            log.error("Error converting to raw amount: {}", e.getMessage());
            return BigInteger.ZERO;
        }
    }
    
    /**
     * Formats a Hedera account ID for display
     * @param accountId account ID as a string
     * @return formatted account ID
     */
    public static String formatAccountId(String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            return "";
        }
        
        // Check if it's already in the 0.0.X format
        if (accountId.contains(".")) {
            return accountId;
        }
        
        try {
            long id = Long.parseLong(accountId);
            return "0.0." + id;
        } catch (NumberFormatException e) {
            return accountId;
        }
    }
    
    /**
     * Formats a Hedera token ID for display
     * @param tokenId token ID as a string
     * @return formatted token ID
     */
    public static String formatTokenId(String tokenId) {
        if (tokenId == null || tokenId.isEmpty()) {
            return "";
        }
        
        // Check if it's already in the 0.0.X format
        if (tokenId.contains(".")) {
            return tokenId;
        }
        
        try {
            long id = Long.parseLong(tokenId);
            return "0.0." + id;
        } catch (NumberFormatException e) {
            return tokenId;
        }
    }
    
    /**
     * Truncates a string if it's longer than the specified length
     * @param str string to truncate
     * @param length maximum length
     * @return truncated string
     */
    public static String truncate(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length) + "...";
    }
} 