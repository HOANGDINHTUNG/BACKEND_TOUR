package com.wedservice.backend.module.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("male"),
    FEMALE("female"),
    OTHER("other"),
    UNKNOWN("unknown");

    private final String value;

    @JsonValue
    public String getJsonValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        for (Gender gender : values()) {
            if (gender.value.equalsIgnoreCase(rawValue) || gender.name().equalsIgnoreCase(rawValue)) {
                return gender;
            }
        }

        throw new IllegalArgumentException("Invalid gender: " + rawValue);
    }
}
