package com.wedservice.backend.module.commerce.entity;

import java.util.Arrays;

public enum ProductType {
    GEAR("gear"),
    INSURANCE("insurance"),
    FOOD("food"),
    SOUVENIR("souvenir"),
    SERVICE("service"),
    GIFT("gift");

    private final String value;

    ProductType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProductType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product type: " + value));
    }
}
