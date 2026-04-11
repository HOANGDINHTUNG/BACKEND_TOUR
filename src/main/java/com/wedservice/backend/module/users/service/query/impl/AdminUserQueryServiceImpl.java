package com.wedservice.backend.module.users.service.query.impl;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.UserSearchRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.AdminUserService;
import com.wedservice.backend.module.users.service.query.AdminUserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserQueryServiceImpl implements AdminUserQueryService {

    private final AdminUserService adminUserService;

    @Override
    public PageResponse<UserResponse> getUsers(UserSearchRequest request) {
        return adminUserService.getUsers(request);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        return adminUserService.getUserById(id);
    }
}
