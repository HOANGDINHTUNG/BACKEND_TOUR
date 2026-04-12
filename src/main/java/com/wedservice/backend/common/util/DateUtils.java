package com.wedservice.backend.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for common date and time operations.
 */
public class DateUtils {

    public static final String DEFAULT_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    /**
     * Formats a LocalDateTime to dd/MM/yyyy HH:mm:ss
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Formats a LocalDateTime to dd/MM/yyyy
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * Custom formatting
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
