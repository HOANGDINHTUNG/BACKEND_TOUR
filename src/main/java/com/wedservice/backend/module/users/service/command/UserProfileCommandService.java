package com.wedservice.backend.module.users.service.command;

import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;

public interface UserProfileCommandService {
    UserResponse updateMyProfile(UpdateMyProfileRequest request);
}
