package com.wedservice.backend.module.reviews.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewSentiment {
    POSITIVE("positive"),
    NEUTRAL("neutral"),
    NEGATIVE("negative"),
    MIXED("mixed");

    private final String value;

    public static ReviewSentiment fromValue(String value) {
        if (value == null || value.isBlank()) {
            return NEUTRAL;
        }
        for (ReviewSentiment sentiment : values()) {
            if (sentiment.value.equalsIgnoreCase(value)) {
                return sentiment;
            }
        }
        throw new IllegalArgumentException("Invalid sentiment: " + value);
    }
}
