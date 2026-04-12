package com.wedservice.backend.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Utility class for generating URL-safe slugs from strings.
 * Especially useful for Vietnamese text normalization.
 */
public class SlugUtils {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s_-]+");
    private static final Pattern REPEATED_HYPHENS = Pattern.compile("-+");

    /**
     * Converts a string to a URL-friendly slug.
     * Example: "Hạ Long Bay" -> "ha-long-bay"
     */
    public static String toSlug(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }

        // 1. Remove accents (Normalize Vietnamese characters)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String result = normalized.replaceAll("\\p{M}", "");

        // 2. Map Đ/đ specifically if needed (optional depending on use case)
        result = result.replace('đ', 'd').replace('Đ', 'D');

        // 3. To lower case
        result = result.toLowerCase(Locale.ENGLISH);

        // 4. Remove non-alphanumeric characters (except whitespace and hyphens)
        result = NON_ALPHANUMERIC.matcher(result).replaceAll("");

        // 5. Replace whitespace/underscores with single hyphen
        result = WHITESPACE.matcher(result).replaceAll("-");

        // 6. Remove repeated hyphens and trim
        result = REPEATED_HYPHENS.matcher(result).replaceAll("-");
        
        // 7. Trim start and end hyphens
        if (result.startsWith("-")) result = result.substring(1);
        if (result.endsWith("-")) result = result.substring(0, result.length() - 1);

        return result;
    }
}
