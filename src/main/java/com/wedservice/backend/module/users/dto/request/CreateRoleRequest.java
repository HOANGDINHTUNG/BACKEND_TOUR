package com.wedservice.backend.module.users.dto.request;

import com.wedservice.backend.module.users.entity.RoleScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

    @NotBlank(message = "code is required")
    @Size(max = 50, message = "code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must not exceed 120 characters")
    private String name;

    @Size(max = 255, message = "description must not exceed 255 characters")
    private String description;

    @NotNull(message = "roleScope is required")
    private RoleScope roleScope;

    @NotNull(message = "hierarchyLevel is required")
    @Min(value = 0, message = "hierarchyLevel must be >= 0")
    @Max(value = 9999, message = "hierarchyLevel must be <= 9999")
    private Integer hierarchyLevel;

    private Boolean isSystemRole;
    private Boolean isActive;
}
