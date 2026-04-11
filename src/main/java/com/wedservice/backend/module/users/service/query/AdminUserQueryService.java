package com.wedservice.backend.module.users.service.query;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;

import java.util.UUID;

public interface AdminUserQueryService {
    PageResponse<UserResponse> getUsers(com.wedservice.backend.module.users.dto.request.UserSearchRequest request);
    UserResponse getUserById(UUID id);
}
