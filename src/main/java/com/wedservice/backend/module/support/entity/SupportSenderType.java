package com.wedservice.backend.module.support.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportSenderType {
    CUSTOMER("customer"),
    STAFF("staff"),
    BOT("bot"),
    SYSTEM("system");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static SupportSenderType fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        for (SupportSenderType type : values()) {
            if (type.value.equalsIgnoreCase(rawValue) || type.name().equalsIgnoreCase(rawValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid support sender type: " + rawValue);
    }
}
