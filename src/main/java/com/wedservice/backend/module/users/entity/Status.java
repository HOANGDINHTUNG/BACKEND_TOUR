package com.wedservice.backend.module.users.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    PENDING("pending"),
    ACTIVE("active"),
    SUSPENDED("suspended"),
    BLOCKED("blocked"),
    DELETED("deleted");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static Status fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (Status status : values()) {
            if (status.value.equalsIgnoreCase(rawValue) || status.name().equalsIgnoreCase(rawValue)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid status: " + rawValue);
    }
}
