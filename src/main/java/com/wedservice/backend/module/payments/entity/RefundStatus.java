package com.wedservice.backend.module.payments.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundStatus {
    REQUESTED("requested"),
    QUOTED("quoted"),
    APPROVED("approved"),
    REJECTED("rejected"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static RefundStatus fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        for (RefundStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue) || status.name().equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid refund status: " + rawValue);
    }
}
