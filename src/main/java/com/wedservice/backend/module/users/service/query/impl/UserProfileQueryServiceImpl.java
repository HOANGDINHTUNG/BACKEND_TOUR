package com.wedservice.backend.module.users.service.query.impl;

import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.UserProfileService;
import com.wedservice.backend.module.users.service.query.UserProfileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileQueryServiceImpl implements UserProfileQueryService {

    private final UserProfileService userProfileService;

    @Override
    public UserResponse getMyProfile() {
        return userProfileService.getMyProfile();
    }

    @Override
    public List<UserAddressResponse> getMyAddresses() {
        return userProfileService.getMyAddresses();
    }

    @Override
    public List<UserDeviceResponse> getMyDevices() {
        return userProfileService.getMyDevices();
    }

    @Override
    public UserPreferenceResponse getMyPreferences() {
        return userProfileService.getMyPreferences();
    }
}
