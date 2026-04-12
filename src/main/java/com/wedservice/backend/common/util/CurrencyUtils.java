package com.wedservice.backend.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility class for formatting currency values.
 */
public class CurrencyUtils {

    private static final Locale VIETNAMESE_LOCALE = new Locale.Builder().setLanguage("vi").setRegion("VN").build();
    private static final Locale US_LOCALE = Locale.US;

    /**
     * Formats a BigDecimal as Vietnamese Dong (VND).
     * Example: 1500000 -> "1.500.000 ₫"
     */
    public static String formatVND(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(VIETNAMESE_LOCALE);
        return formatter.format(amount);
    }

    /**
     * Formats a BigDecimal as US Dollars (USD).
     * Example: 100.5 -> "$100.50"
     */
    public static String formatUSD(BigDecimal amount) {
        if (amount == null) return "$0.00";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(US_LOCALE);
        return formatter.format(amount);
    }

    /**
     * Formats according to a specific currency code (VND, USD, etc.)
     */
    public static String format(BigDecimal amount, String currencyCode) {
        if (amount == null) return "0";
        if ("VND".equalsIgnoreCase(currencyCode)) return formatVND(amount);
        if ("USD".equalsIgnoreCase(currencyCode)) return formatUSD(amount);
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        try {
            formatter.setCurrency(Currency.getInstance(currencyCode));
        } catch (Exception e) {
            // Fallback to default formatting if code is invalid
        }
        return formatter.format(amount);
    }
}
