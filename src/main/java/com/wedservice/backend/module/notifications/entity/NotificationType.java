package com.wedservice.backend.module.notifications.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    BOOKING("booking"),
    PAYMENT("payment"),
    WEATHER("weather"),
    PROMOTION("promotion"),
    SCHEDULE_CHANGE("schedule_change"),
    REMINDER("reminder"),
    SYSTEM("system"),
    CHAT("chat"),
    SUPPORT("support"),
    DESTINATION_FOLLOW("destination_follow");

    private final String value;

    public static NotificationType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown notification type: " + value));
    }
}
