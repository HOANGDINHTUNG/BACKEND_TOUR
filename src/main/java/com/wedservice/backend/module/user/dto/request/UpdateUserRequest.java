package com.wedservice.backend.module.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phone;

    @NotBlank(message = "Role is required")
    private String role;
}
