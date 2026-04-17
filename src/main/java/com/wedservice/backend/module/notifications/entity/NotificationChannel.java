package com.wedservice.backend.module.notifications.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum NotificationChannel {
    PUSH("push"),
    EMAIL("email"),
    SMS("sms"),
    IN_APP("in_app");

    private final String value;

    public static NotificationChannel fromValue(String value) {
        return Arrays.stream(values())
                .filter(channel -> channel.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown notification channel: " + value));
    }
}
