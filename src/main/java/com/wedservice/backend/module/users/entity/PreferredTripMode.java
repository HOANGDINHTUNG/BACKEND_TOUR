package com.wedservice.backend.module.users.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PreferredTripMode {
    GROUP("group"),
    PRIVATE("private"),
    SHARED("shared");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static PreferredTripMode fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (PreferredTripMode mode : values()) {
            if (mode.value.equalsIgnoreCase(rawValue) || mode.name().equalsIgnoreCase(rawValue)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Invalid preferred trip mode: " + rawValue);
    }
}
