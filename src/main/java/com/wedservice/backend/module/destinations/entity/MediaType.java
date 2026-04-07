package com.wedservice.backend.module.destinations.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType {
    IMAGE("image"),
    VIDEO("video"),
    COVER("cover"),
    BANNER("banner");

    private final String value;

    public static MediaType fromValue(String value) {
        for (MediaType type : MediaType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return IMAGE;
    }
}
