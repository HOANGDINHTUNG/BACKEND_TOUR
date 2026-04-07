package com.wedservice.backend.module.users.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberLevel {
    BRONZE("bronze"),
    SILVER("silver"),
    GOLD("gold"),
    PLATINUM("platinum"),
    DIAMOND("diamond");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static MemberLevel fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (MemberLevel memberLevel : values()) {
            if (memberLevel.value.equalsIgnoreCase(rawValue) || memberLevel.name().equalsIgnoreCase(rawValue)) {
                return memberLevel;
            }
        }

        throw new IllegalArgumentException("Invalid member level: " + rawValue);
    }
}
