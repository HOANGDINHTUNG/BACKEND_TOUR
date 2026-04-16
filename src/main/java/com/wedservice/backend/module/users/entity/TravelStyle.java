package com.wedservice.backend.module.users.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TravelStyle {
    RELAX("relax"),
    ADVENTURE("adventure"),
    CHECKIN("checkin"),
    FAMILY("family"),
    CULTURE("culture"),
    FOOD("food"),
    SPIRITUAL("spiritual"),
    MIXED("mixed");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static TravelStyle fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (TravelStyle style : values()) {
            if (style.value.equalsIgnoreCase(rawValue) || style.name().equalsIgnoreCase(rawValue)) {
                return style;
            }
        }

        throw new IllegalArgumentException("Invalid travel style: " + rawValue);
    }
}
