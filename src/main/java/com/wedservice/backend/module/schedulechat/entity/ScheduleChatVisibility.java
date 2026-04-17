package com.wedservice.backend.module.schedulechat.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleChatVisibility {
    ALL_MEMBERS("all_members"),
    STAFF_ONLY("staff_only");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static ScheduleChatVisibility fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        for (ScheduleChatVisibility visibility : values()) {
            if (visibility.value.equalsIgnoreCase(rawValue) || visibility.name().equalsIgnoreCase(rawValue)) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Invalid schedule chat visibility: " + rawValue);
    }
}
