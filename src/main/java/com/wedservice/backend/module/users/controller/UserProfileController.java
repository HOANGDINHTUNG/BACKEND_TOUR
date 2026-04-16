package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.facade.UserProfileFacade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/addresses")
    public ApiResponse<List<UserAddressResponse>> getMyAddresses() {
        return ApiResponse.<List<UserAddressResponse>>builder()
                .success(true)
                .message("User addresses fetched successfully")
                .data(userProfileFacade.getMyAddresses())
                .build();
    }

    @GetMapping("/preferences")
    public ApiResponse<UserPreferenceResponse> getMyPreferences() {
        return ApiResponse.<UserPreferenceResponse>builder()
                .success(true)
                .message("User preferences fetched successfully")
                .data(userProfileFacade.getMyPreferences())
                .build();
    }

    @PutMapping("/preferences")
    public ApiResponse<UserPreferenceResponse> updateMyPreferences(@Valid @RequestBody UserPreferenceRequest request) {
        return ApiResponse.<UserPreferenceResponse>builder()
                .success(true)
                .message("User preferences updated successfully")
                .data(userProfileFacade.updateMyPreferences(request))
                .build();
    }

    @GetMapping("/devices")
    public ApiResponse<List<UserDeviceResponse>> getMyDevices() {
        return ApiResponse.<List<UserDeviceResponse>>builder()
                .success(true)
                .message("User devices fetched successfully")
                .data(userProfileFacade.getMyDevices())
                .build();
    }

    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDeviceResponse> registerMyDevice(@Valid @RequestBody UserDeviceRequest request) {
        return ApiResponse.<UserDeviceResponse>builder()
                .success(true)
                .message("Device registered successfully")
                .data(userProfileFacade.registerMyDevice(request))
                .build();
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserAddressResponse> createMyAddress(@Valid @RequestBody UserAddressRequest request) {
        return ApiResponse.<UserAddressResponse>builder()
                .success(true)
                .message("Address created successfully")
                .data(userProfileFacade.createMyAddress(request))
                .build();
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<UserAddressResponse> updateMyAddress(
            @PathVariable Long id,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return ApiResponse.<UserAddressResponse>builder()
                .success(true)
                .message("Address updated successfully")
                .data(userProfileFacade.updateMyAddress(id, request))
                .build();
    }

    @PatchMapping("/addresses/{id}/default")
    public ApiResponse<UserAddressResponse> setMyDefaultAddress(@PathVariable Long id) {
        return ApiResponse.<UserAddressResponse>builder()
                .success(true)
                .message("Default address updated successfully")
                .data(userProfileFacade.setMyDefaultAddress(id))
                .build();
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteMyAddress(@PathVariable Long id) {
        userProfileFacade.deleteMyAddress(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Address deleted successfully")
                .build();
    }

    @DeleteMapping("/devices/{id}")
    public ApiResponse<Void> deleteMyDevice(@PathVariable Long id) {
        userProfileFacade.deleteMyDevice(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Device removed successfully")
                .build();
    }
}
