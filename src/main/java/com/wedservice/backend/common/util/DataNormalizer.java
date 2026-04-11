package com.wedservice.backend.common.util;

import org.springframework.util.StringUtils;
import java.util.Locale;

public final class DataNormalizer {

    private DataNormalizer() {
    }

    public static String normalize(String value) {
        return (value == null) ? null : value.trim();
    }

    public static String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return phone.trim().replaceAll("[\\s\\-().]", "");
    }

    public static String normalizeLoginIdentifier(String login) {
        if (!StringUtils.hasText(login)) {
            return null;
        }
        String trimmed = login.trim();
        return trimmed.contains("@") ? normalizeEmail(trimmed) : normalizePhone(trimmed);
    }
}
