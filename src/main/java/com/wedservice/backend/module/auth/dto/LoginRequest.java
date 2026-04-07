package com.wedservice.backend.module.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email or phone is required")
    @JsonAlias("email")
    private String login;

    @NotBlank(message = "Password is required")
    private String passwordHash;
}
