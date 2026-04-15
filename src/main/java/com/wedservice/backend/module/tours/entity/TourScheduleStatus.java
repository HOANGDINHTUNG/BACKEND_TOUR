package com.wedservice.backend.module.tours.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TourScheduleStatus {
    DRAFT("draft"),
    OPEN("open"),
    CLOSED("closed"),
    FULL("full"),
    DEPARTED("departed"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static TourScheduleStatus fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        for (TourScheduleStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue) || status.name().equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid tour schedule status: " + rawValue);
    }
}
