package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.facade.UserProfileFacade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-profile endpoints for the currently authenticated user.
 */
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

    private final UserProfileFacade userProfileFacade;

    @GetMapping
    public ApiResponse<UserResponse> getMyProfile() {
        UserResponse response = userProfileFacade.getMyProfile();

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Current user fetched successfully")
                .data(response)
                .build();
    }

    @PutMapping
    public ApiResponse<UserResponse> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        UserResponse response = userProfileFacade.updateMyProfile(request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(response)
                .build();
    }
}
