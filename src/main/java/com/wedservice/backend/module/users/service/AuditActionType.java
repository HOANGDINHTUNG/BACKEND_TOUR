package com.wedservice.backend.module.users.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditActionType {
    ROLE_CREATE("role.create", "roles"),
    ROLE_UPDATE("role.update", "roles"),
    PERMISSION_ASSIGN("permission.assign", "roles"),
    USER_CREATE("user.create", "users"),
    USER_UPDATE("user.update", "users"),
    USER_DEACTIVATE("user.deactivate", "users");

    private final String actionName;
    private final String entityName;
}
