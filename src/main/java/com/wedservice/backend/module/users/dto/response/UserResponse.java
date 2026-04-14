package com.wedservice.backend.module.users.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.wedservice.backend.module.users.entity.Gender;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.UserCategory;

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
    private UserCategory userCategory;
    private String role; // Primary role code
    private java.util.List<String> roles; // All role codes
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
