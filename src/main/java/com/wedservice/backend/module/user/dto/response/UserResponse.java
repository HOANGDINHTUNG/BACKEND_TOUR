package com.wedservice.backend.module.user.dto.response;

import com.wedservice.backend.module.user.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Boolean active;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
