package com.wedservice.backend.module.users.util;

import org.springframework.util.StringUtils;

import java.util.Locale;

public final class UserContactNormalizer {

    private UserContactNormalizer() {
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
