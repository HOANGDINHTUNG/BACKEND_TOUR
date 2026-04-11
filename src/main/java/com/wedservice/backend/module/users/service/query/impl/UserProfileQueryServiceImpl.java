package com.wedservice.backend.module.users.service.query.impl;

import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.UserProfileService;
import com.wedservice.backend.module.users.service.query.UserProfileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileQueryServiceImpl implements UserProfileQueryService {

    private final UserProfileService userProfileService;

    @Override
    public UserResponse getMyProfile() {
        return userProfileService.getMyProfile();
    }
}
