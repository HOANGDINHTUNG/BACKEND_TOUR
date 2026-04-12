package com.wedservice.backend.module.users.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import com.wedservice.backend.module.users.entity.converter.GenderConverter;
import com.wedservice.backend.module.users.entity.converter.MemberLevelConverter;
import com.wedservice.backend.module.users.entity.converter.RoleConverter;
import com.wedservice.backend.module.users.entity.converter.StatusConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false, updatable = false, length = 36)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    private UUID id;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Convert(converter = RoleConverter.class)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    @Convert(converter = StatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Convert(converter = GenderConverter.class)
    @Column(name = "gender", nullable = false, length = 20)
    @Builder.Default
    private Gender gender = Gender.UNKNOWN;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Convert(converter = MemberLevelConverter.class)
    @Column(name = "member_level", nullable = false, length = 20)
    @Builder.Default
    private MemberLevel memberLevel = MemberLevel.BRONZE;

    @Column(name = "loyalty_points", nullable = false)
    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Column(name = "total_spent", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @jakarta.persistence.PrePersist
    protected void beforeInsert() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        applyDefaults();
        validateState();
    }

    @jakarta.persistence.PreUpdate
    protected void beforeUpdate() {
        applyDefaults();
        validateState();
    }

    private void applyDefaults() {
        if (role == null) role = Role.CUSTOMER;
        if (status == null) status = Status.ACTIVE;
        if (gender == null) gender = Gender.UNKNOWN;
        if (memberLevel == null) memberLevel = MemberLevel.BRONZE;
        if (loyaltyPoints == null) loyaltyPoints = 0;
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;
    }

    private void validateState() {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new IllegalStateException("User must have at least email or phone");
        }
    }
}
