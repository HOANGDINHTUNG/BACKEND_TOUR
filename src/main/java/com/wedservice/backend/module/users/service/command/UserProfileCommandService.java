package com.wedservice.backend.module.users.service.command;

import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;

public interface UserProfileCommandService {
    UserResponse updateMyProfile(UpdateMyProfileRequest request);
    UserAddressResponse createMyAddress(UserAddressRequest request);
    UserDeviceResponse registerMyDevice(UserDeviceRequest request);
    UserPreferenceResponse updateMyPreferences(UserPreferenceRequest request);
    UserAddressResponse updateMyAddress(Long id, UserAddressRequest request);
    UserAddressResponse setMyDefaultAddress(Long id);
    void deleteMyAddress(Long id);
    void deleteMyDevice(Long id);
}
