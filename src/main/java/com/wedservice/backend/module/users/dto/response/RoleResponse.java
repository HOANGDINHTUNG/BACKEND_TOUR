package com.wedservice.backend.module.users.dto.response;

import com.wedservice.backend.module.users.entity.RoleScope;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private RoleScope roleScope;
    private Integer hierarchyLevel;
    private Boolean isSystemRole;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PermissionResponse> permissions;
}
