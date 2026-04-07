package com.wedservice.backend.module.auth.dto;

import com.wedservice.backend.module.user.entity.Gender;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Data
public class RegisterRequest {

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

    @Size(max = 120, message = "Display name must not exceed 120 characters")
    private String displayName;

    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String avatarUrl;

    @AssertTrue(message = "At least email or phone must be provided")
    public boolean isContactProvided() {
        return StringUtils.hasText(email) || StringUtils.hasText(phone);
    }
}
