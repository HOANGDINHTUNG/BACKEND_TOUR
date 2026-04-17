package com.wedservice.backend.module.promotions.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoucherDiscountType {
    PERCENTAGE("percentage"),
    FIXED_AMOUNT("fixed_amount"),
    GIFT("gift"),
    CASHBACK("cashback");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static VoucherDiscountType fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (VoucherDiscountType discountType : values()) {
            if (discountType.value.equalsIgnoreCase(rawValue) || discountType.name().equalsIgnoreCase(rawValue)) {
                return discountType;
            }
        }

        throw new IllegalArgumentException("Invalid voucher discount type: " + rawValue);
    }
}
