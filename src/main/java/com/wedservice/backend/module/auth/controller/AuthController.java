package com.wedservice.backend.module.auth.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.LoginRequest;
import com.wedservice.backend.module.auth.dto.RegisterRequest;
import com.wedservice.backend.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Register successfully")
                .data(response)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successfully")
                .data(response)
                .build();
    }
}
