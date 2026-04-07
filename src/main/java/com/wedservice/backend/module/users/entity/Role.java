package com.wedservice.backend.module.users.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    CUSTOMER("customer"),
    STAFF("staff"),
    ADMIN("admin"),
    MANAGER("manager");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static Role fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (Role role : values()) {
            if (role.value.equalsIgnoreCase(rawValue) || role.name().equalsIgnoreCase(rawValue)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Invalid role: " + rawValue);
    }
}
