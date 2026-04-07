package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.request.UserSearchRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserResponse response = adminUserService.createUser(request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User created successfully")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(@Valid @ModelAttribute UserSearchRequest request) {
        PageResponse<UserResponse> users = adminUserService.getUsers(request);

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("User list fetched successfully")
                .data(users)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = adminUserService.getUserById(id);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User fetched successfully")
                .data(user)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        UserResponse updatedUser = adminUserService.updateUser(id, request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User updated successfully")
                .data(updatedUser)
                .build();
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<UserResponse> deactivateUser(@PathVariable UUID id) {
        UserResponse deactivatedUser = adminUserService.deactivateUser(id);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User deactivated successfully")
                .data(deactivatedUser)
                .build();
    }
}
