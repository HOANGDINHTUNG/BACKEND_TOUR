package com.wedservice.backend.module.users.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.Gender;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;

import java.math.BigDecimal;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .displayName(user.getDisplayName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .memberLevel(user.getMemberLevel())
                .loyaltyPoints(user.getLoyaltyPoints())
                .totalSpent(user.getTotalSpent())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .phoneVerifiedAt(user.getPhoneVerifiedAt())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    public User toNewUser(
            AdminCreateUserRequest request,
            String normalizedEmail,
            String normalizedPhone,
            String encodedPassword
    ) {
        return User.builder()
                .fullName(normalizeRequired(request.getFullName()))
                .displayName(resolveDisplayName(request.getDisplayName(), request.getFullName()))
                .email(normalizedEmail)
                .phone(normalizedPhone)
                .passwordHash(encodedPassword)
                .role(defaultRole(request.getRole()))
                .status(defaultStatus(request.getStatus()))
                .gender(defaultGender(request.getGender()))
                .dateOfBirth(request.getDateOfBirth())
                .avatarUrl(normalizeNullable(request.getAvatarUrl()))
                .memberLevel(defaultMemberLevel(request.getMemberLevel()))
                .loyaltyPoints(defaultInteger(request.getLoyaltyPoints()))
                .totalSpent(defaultBigDecimal(request.getTotalSpent()))
                .emailVerifiedAt(request.getEmailVerifiedAt())
                .phoneVerifiedAt(request.getPhoneVerifiedAt())
                .lastLoginAt(request.getLastLoginAt())
                .build();
    }

    public void applyAdminUpdate(
            User user,
            AdminUpdateUserRequest request,
            String normalizedEmail,
            String normalizedPhone,
            String encodedPassword
    ) {
        user.setFullName(normalizeRequired(request.getFullName()));
        user.setDisplayName(resolveDisplayName(request.getDisplayName(), request.getFullName()));
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setGender(defaultGender(request.getGender()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAvatarUrl(normalizeNullable(request.getAvatarUrl()));
        user.setMemberLevel(defaultMemberLevel(request.getMemberLevel()));
        user.setLoyaltyPoints(defaultInteger(request.getLoyaltyPoints()));
        user.setTotalSpent(defaultBigDecimal(request.getTotalSpent()));
        user.setEmailVerifiedAt(request.getEmailVerifiedAt());
        user.setPhoneVerifiedAt(request.getPhoneVerifiedAt());
        user.setLastLoginAt(request.getLastLoginAt());
        user.setDeletedAt(request.getDeletedAt());

        if (StringUtils.hasText(encodedPassword)) {
            user.setPasswordHash(encodedPassword);
        }
    }

    public void applyProfileUpdate(
            User user,
            UpdateMyProfileRequest request,
            String normalizedEmail,
            String normalizedPhone
    ) {
        user.setFullName(normalizeRequired(request.getFullName()));
        user.setDisplayName(resolveDisplayName(request.getDisplayName(), request.getFullName()));
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setGender(defaultGender(request.getGender()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAvatarUrl(normalizeNullable(request.getAvatarUrl()));
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveDisplayName(String displayName, String fullName) {
        return StringUtils.hasText(displayName) ? displayName.trim() : normalizeRequired(fullName);
    }

    private Role defaultRole(Role role) {
        return role == null ? Role.CUSTOMER : role;
    }

    private Status defaultStatus(Status status) {
        return status == null ? Status.ACTIVE : status;
    }

    private Gender defaultGender(Gender gender) {
        return gender == null ? Gender.UNKNOWN : gender;
    }

    private MemberLevel defaultMemberLevel(MemberLevel memberLevel) {
        return memberLevel == null ? MemberLevel.BRONZE : memberLevel;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
