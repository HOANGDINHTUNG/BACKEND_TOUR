package com.wedservice.backend.module.promotions.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoucherApplicableScope {
    ALL("all"),
    TOUR("tour"),
    DESTINATION("destination");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static VoucherApplicableScope fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (VoucherApplicableScope scope : values()) {
            if (scope.value.equalsIgnoreCase(rawValue) || scope.name().equalsIgnoreCase(rawValue)) {
                return scope;
            }
        }

        throw new IllegalArgumentException("Invalid voucher applicable scope: " + rawValue);
    }
}
