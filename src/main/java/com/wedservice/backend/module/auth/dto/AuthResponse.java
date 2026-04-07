package com.wedservice.backend.module.auth.dto;

import com.wedservice.backend.module.users.dto.response.UserResponse;

import lombok.Builder;
import lombok.Data;

/**
 * Authentication result returned after register/login.
 */
@Data
@Builder
public class AuthResponse {
    private UserResponse user;
    private String tokenType;
    private String accessToken;
    private long expiresIn;
}
