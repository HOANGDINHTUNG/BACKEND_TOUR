package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.request.UserSearchRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.facade.AdminUserFacade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final AdminUserFacade adminUserFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserResponse response = adminUserFacade.createUser(request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User created successfully")
                .data(response)
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<PageResponse<UserResponse>> getUsers(@Valid @ModelAttribute UserSearchRequest request) {
        PageResponse<UserResponse> users = adminUserFacade.getUsers(request);

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("User list fetched successfully")
                .data(users)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = adminUserFacade.getUserById(id);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User fetched successfully")
                .data(user)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        UserResponse updatedUser = adminUserFacade.updateUser(id, request);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User updated successfully")
                .data(updatedUser)
                .build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('user.block','user.delete')")
    public ApiResponse<UserResponse> deactivateUser(@PathVariable UUID id) {
        UserResponse deactivatedUser = adminUserFacade.deactivateUser(id);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User deactivated successfully")
                .data(deactivatedUser)
                .build();
    }
}
