package com.wedservice.backend.module.payments.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
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
    public static PaymentStatus fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        for (PaymentStatus status : values()) {
            if (status.value.equalsIgnoreCase(rawValue) || status.name().equalsIgnoreCase(rawValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid payment status: " + rawValue);
    }
}
