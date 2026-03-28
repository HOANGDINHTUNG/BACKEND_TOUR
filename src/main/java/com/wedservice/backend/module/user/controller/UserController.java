package com.wedservice.backend.module.user.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.user.dto.request.CreateUserRequest;
import com.wedservice.backend.module.user.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.user.dto.request.UpdateUserRequest;
import com.wedservice.backend.module.user.dto.response.UserResponse;
import com.wedservice.backend.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User created successfully")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active
    ) {
        PageResponse<UserResponse> users = userService.getUsers(page, size, keyword, active);

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("User list fetched successfully")
                .data(users)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User fetched successfully")
                .data(user)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse updatedUser = userService.updateUser(id, request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User updated successfully")
                .data(updatedUser)
                .build();
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<UserResponse> deactivateUser(@PathVariable Long id) {
        UserResponse deactivatedUser = userService.deactivateUser(id);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User deactivated successfully")
                .data(deactivatedUser)
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser == null ? null : currentUser.getUserId();
        UserResponse response = userService.getMyProfile(userId);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Current user fetched successfully")
                .data(response)
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        Long userId = currentUser == null ? null : currentUser.getUserId();
        UserResponse response = userService.updateMyProfile(userId, request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(response)
                .build();
    }
}
