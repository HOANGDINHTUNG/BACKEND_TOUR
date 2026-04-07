package com.wedservice.backend.module.destinations.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrowdLevel {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    VERY_HIGH("very_high");

    private final String value;

    public static CrowdLevel fromValue(String value) {
        for (CrowdLevel level : CrowdLevel.values()) {
            if (level.value.equalsIgnoreCase(value)) {
                return level;
            }
        }
        return MEDIUM;
    }
}
