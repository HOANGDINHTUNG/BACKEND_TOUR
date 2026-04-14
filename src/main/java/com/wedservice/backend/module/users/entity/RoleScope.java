package com.wedservice.backend.module.users.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleScope {
    SYSTEM("SYSTEM"),
    BACKOFFICE("BACKOFFICE"),
    CUSTOMER("CUSTOMER");

    private final String value;
}
