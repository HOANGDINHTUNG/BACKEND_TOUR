package com.wedservice.backend.module.users.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import com.wedservice.backend.module.users.entity.Gender;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdminCreateUserRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Email(message = "Email is invalid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{8,20}$", message = "Phone number is invalid")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String passwordHash;

    @NotNull(message = "Role is required")
    private Role role;

    private Status status = Status.ACTIVE;

    @Size(max = 120, message = "Display name must not exceed 120 characters")
    private String displayName;

    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String avatarUrl;

    private MemberLevel memberLevel = MemberLevel.BRONZE;

    @PositiveOrZero(message = "Loyalty points must be greater than or equal to 0")
    private Integer loyaltyPoints;

    @PositiveOrZero(message = "Total spent must be greater than or equal to 0")
    private BigDecimal totalSpent;

    private LocalDateTime emailVerifiedAt;
    private LocalDateTime phoneVerifiedAt;
    private LocalDateTime lastLoginAt;

    @AssertTrue(message = "At least email or phone must be provided")
    public boolean isContactProvided() {
        return StringUtils.hasText(email) || StringUtils.hasText(phone);
    }
}
