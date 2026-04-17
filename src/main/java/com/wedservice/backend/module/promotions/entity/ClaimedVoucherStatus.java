package com.wedservice.backend.module.promotions.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimedVoucherStatus {
    AVAILABLE("available"),
    INACTIVE("inactive"),
    EXPIRED("expired"),
    EXHAUSTED_TOTAL("exhausted_total"),
    USED_UP("used_up");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }
}
