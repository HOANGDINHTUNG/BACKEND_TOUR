package com.wedservice.backend.module.bookings.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingPaymentStatus {
    UNPAID("unpaid"),
    PARTIAL("partial"),
    PAID("paid"),
    FAILED("failed"),
    REFUNDED("refunded"),
    CHARGEBACK("chargeback");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static BookingPaymentStatus fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        for (BookingPaymentStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue) || status.name().equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid booking payment status: " + rawValue);
    }
}
