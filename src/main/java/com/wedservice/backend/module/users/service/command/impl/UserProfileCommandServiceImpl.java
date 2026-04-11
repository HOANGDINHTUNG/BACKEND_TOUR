package com.wedservice.backend.module.users.service.command.impl;

import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
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
}
