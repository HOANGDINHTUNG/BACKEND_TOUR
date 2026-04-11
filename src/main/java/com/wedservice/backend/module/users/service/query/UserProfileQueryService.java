package com.wedservice.backend.module.users.service.query;

import com.wedservice.backend.module.users.dto.response.UserResponse;

public interface UserProfileQueryService {
    UserResponse getMyProfile();
}
