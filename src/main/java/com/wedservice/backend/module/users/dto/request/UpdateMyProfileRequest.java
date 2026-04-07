package com.wedservice.backend.module.users.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import com.wedservice.backend.module.users.entity.Gender;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateMyProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Email(message = "Email is invalid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{8,20}$", message = "Phone number is invalid")
    private String phone;

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
