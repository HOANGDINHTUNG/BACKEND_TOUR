package com.wedservice.backend.module.users.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BudgetLevel {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    LUXURY("luxury");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static BudgetLevel fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (BudgetLevel level : values()) {
            if (level.value.equalsIgnoreCase(rawValue) || level.name().equalsIgnoreCase(rawValue)) {
                return level;
            }
        }

        throw new IllegalArgumentException("Invalid budget level: " + rawValue);
    }
}
