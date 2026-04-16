package com.wedservice.backend.module.users.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PermissionResponse {
    private Long id;
    private String code;
    private String name;
    private String moduleName;
    private String actionName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
