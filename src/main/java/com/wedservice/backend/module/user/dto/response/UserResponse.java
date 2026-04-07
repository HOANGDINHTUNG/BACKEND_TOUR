package com.wedservice.backend.module.user.dto.response;

import com.wedservice.backend.module.user.entity.Gender;
import com.wedservice.backend.module.user.entity.MemberLevel;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String fullName;
    private String displayName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private Role role;
    private Status status;
    private MemberLevel memberLevel;
    private Integer loyaltyPoints;
    private BigDecimal totalSpent;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime phoneVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
