package com.wedservice.backend.module.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.wedservice.backend.common.mapper.BaseMapper;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.Gender;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DataNormalizer.class})
public interface UserMapper extends BaseMapper<UserResponse, User> {

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", expression = "java(user.getRoleName())")
    @Mapping(target = "roles", expression = "java(user.getUserRoles().stream().map(ur -> ur.getRole().getCode()).toList())")
    UserResponse toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    User toEntity(UserResponse dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "fullName", expression = "java(DataNormalizer.normalize(request.getFullName()))")
    @Mapping(target = "displayName", expression = "java(resolveDisplayName(request.getDisplayName(), request.getFullName()))")
    @Mapping(target = "email", source = "normalizedEmail")
    @Mapping(target = "phone", source = "normalizedPhone")
    @Mapping(target = "passwordHash", source = "encodedPassword")
    @Mapping(target = "userCategory", source = "request.userCategory")
    @Mapping(target = "status", expression = "java(defaultStatus(request.getStatus()))")
    @Mapping(target = "gender", expression = "java(defaultGender(request.getGender()))")
    @Mapping(target = "avatarUrl", expression = "java(DataNormalizer.normalize(request.getAvatarUrl()))")
    @Mapping(target = "memberLevel", expression = "java(defaultMemberLevel(request.getMemberLevel()))")
    @Mapping(target = "loyaltyPoints", expression = "java(defaultInteger(request.getLoyaltyPoints()))")
    @Mapping(target = "totalSpent", expression = "java(defaultBigDecimal(request.getTotalSpent()))")
    User toNewUser(
            AdminCreateUserRequest request,
            String normalizedEmail,
            String normalizedPhone,
            String encodedPassword
    );

    @Mapping(target = "fullName", expression = "java(DataNormalizer.normalize(request.getFullName()))")
    @Mapping(target = "displayName", expression = "java(resolveDisplayName(request.getDisplayName(), request.getFullName()))")
    @Mapping(target = "email", source = "normalizedEmail")
    @Mapping(target = "phone", source = "normalizedPhone")
    @Mapping(target = "passwordHash", source = "encodedPassword")
    @Mapping(target = "gender", expression = "java(defaultGender(request.getGender()))")
    @Mapping(target = "avatarUrl", expression = "java(DataNormalizer.normalize(request.getAvatarUrl()))")
    @Mapping(target = "memberLevel", expression = "java(defaultMemberLevel(request.getMemberLevel()))")
    @Mapping(target = "loyaltyPoints", expression = "java(defaultInteger(request.getLoyaltyPoints()))")
    @Mapping(target = "totalSpent", expression = "java(defaultBigDecimal(request.getTotalSpent()))")
    @Mapping(target = "userRoles", ignore = true)
    void applyAdminUpdate(
            @MappingTarget User user,
            AdminUpdateUserRequest request,
            String normalizedEmail,
            String normalizedPhone,
            String encodedPassword
    );

    @Mapping(target = "fullName", expression = "java(DataNormalizer.normalize(request.getFullName()))")
    @Mapping(target = "displayName", expression = "java(resolveDisplayName(request.getDisplayName(), request.getFullName()))")
    @Mapping(target = "email", source = "normalizedEmail")
    @Mapping(target = "phone", source = "normalizedPhone")
    @Mapping(target = "gender", expression = "java(defaultGender(request.getGender()))")
    @Mapping(target = "avatarUrl", expression = "java(DataNormalizer.normalize(request.getAvatarUrl()))")
    void applyProfileUpdate(
            @MappingTarget User user,
            UpdateMyProfileRequest request,
            String normalizedEmail,
            String normalizedPhone
    );

    default String resolveDisplayName(String displayName, String fullName) {
        return org.springframework.util.StringUtils.hasText(displayName) ? displayName.trim() : DataNormalizer.normalize(fullName);
    }

    default Status defaultStatus(Status status) {
        return status == null ? Status.ACTIVE : status;
    }

    default Gender defaultGender(Gender gender) {
        return gender == null ? Gender.UNKNOWN : gender;
    }

    default MemberLevel defaultMemberLevel(MemberLevel memberLevel) {
        return memberLevel == null ? MemberLevel.BRONZE : memberLevel;
    }

    default Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    default BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
