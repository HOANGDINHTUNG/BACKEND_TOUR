package com.wedservice.backend.module.bookings.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingStatus {
    PENDING_PAYMENT("pending_payment"),
    CONFIRMED("confirmed"),
    CHECKED_IN("checked_in"),
    COMPLETED("completed"),
    CANCEL_REQUESTED("cancel_requested"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    EXPIRED("expired");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static BookingStatus fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        for (BookingStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue) || status.name().equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid booking status: " + rawValue);
    }
}
