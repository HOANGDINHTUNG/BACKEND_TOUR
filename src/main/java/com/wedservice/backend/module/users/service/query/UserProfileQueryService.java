package com.wedservice.backend.module.users.service.query;

import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;

import java.util.List;

public interface UserProfileQueryService {
    UserResponse getMyProfile();
    List<UserAddressResponse> getMyAddresses();
    List<UserDeviceResponse> getMyDevices();
    UserPreferenceResponse getMyPreferences();
}
