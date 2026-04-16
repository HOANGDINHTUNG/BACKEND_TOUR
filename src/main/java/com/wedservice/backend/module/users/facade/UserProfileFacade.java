package com.wedservice.backend.module.users.facade;

import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.command.UserProfileCommandService;
import com.wedservice.backend.module.users.service.query.UserProfileQueryService;
import com.wedservice.backend.module.users.validator.UserProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserProfileFacade {

    private final UserProfileCommandService commandService;
    private final UserProfileQueryService queryService;
    private final UserProfileValidator validator;

    public UserResponse getMyProfile() {
        return queryService.getMyProfile();
    }

    public List<UserAddressResponse> getMyAddresses() {
        return queryService.getMyAddresses();
    }

    public UserPreferenceResponse getMyPreferences() {
        return queryService.getMyPreferences();
    }

    public List<UserDeviceResponse> getMyDevices() {
        return queryService.getMyDevices();
    }

    public UserResponse updateMyProfile(UpdateMyProfileRequest request) {
        String email = request.getEmail();
        String phone = request.getPhone();
        validator.validateRequiredContact(email, phone);
        return commandService.updateMyProfile(request);
    }

    public UserAddressResponse createMyAddress(UserAddressRequest request) {
        return commandService.createMyAddress(request);
    }

    public UserPreferenceResponse updateMyPreferences(UserPreferenceRequest request) {
        return commandService.updateMyPreferences(request);
    }

    public UserDeviceResponse registerMyDevice(UserDeviceRequest request) {
        return commandService.registerMyDevice(request);
    }

    public UserAddressResponse updateMyAddress(Long id, UserAddressRequest request) {
        return commandService.updateMyAddress(id, request);
    }

    public UserAddressResponse setMyDefaultAddress(Long id) {
        return commandService.setMyDefaultAddress(id);
    }

    public void deleteMyAddress(Long id) {
        commandService.deleteMyAddress(id);
    }

    public void deleteMyDevice(Long id) {
        commandService.deleteMyDevice(id);
    }
}
