package com.wedservice.backend.module.users.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserCategory {
    INTERNAL("INTERNAL"),
    CUSTOMER("CUSTOMER");

    private final String value;
}
