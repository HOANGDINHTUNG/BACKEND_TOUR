package com.wedservice.backend.module.users.service.command.impl;

import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.UserProfileService;
import com.wedservice.backend.module.users.service.command.UserProfileCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileCommandServiceImpl implements UserProfileCommandService {

    private final UserProfileService userProfileService;

    @Override
    public UserResponse updateMyProfile(UpdateMyProfileRequest request) {
        return userProfileService.updateMyProfile(request);
    }

    @Override
    public UserAddressResponse createMyAddress(UserAddressRequest request) {
        return userProfileService.createMyAddress(request);
    }

    @Override
    public UserDeviceResponse registerMyDevice(UserDeviceRequest request) {
        return userProfileService.registerMyDevice(request);
    }

    @Override
    public UserPreferenceResponse updateMyPreferences(UserPreferenceRequest request) {
        return userProfileService.updateMyPreferences(request);
    }

    @Override
    public UserAddressResponse updateMyAddress(Long id, UserAddressRequest request) {
        return userProfileService.updateMyAddress(id, request);
    }

    @Override
    public UserAddressResponse setMyDefaultAddress(Long id) {
        return userProfileService.setMyDefaultAddress(id);
    }

    @Override
    public void deleteMyAddress(Long id) {
        userProfileService.deleteMyAddress(id);
    }

    @Override
    public void deleteMyDevice(Long id) {
        userProfileService.deleteMyDevice(id);
    }
}
