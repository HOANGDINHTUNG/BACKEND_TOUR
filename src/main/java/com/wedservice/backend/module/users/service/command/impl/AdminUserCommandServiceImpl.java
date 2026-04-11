package com.wedservice.backend.module.users.service.command.impl;

import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.AdminUserService;
import com.wedservice.backend.module.users.service.command.AdminUserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserCommandServiceImpl implements AdminUserCommandService {

    private final AdminUserService adminUserService;

    @Override
    public UserResponse createUser(AdminCreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    @Override
    public UserResponse updateUser(UUID id, AdminUpdateUserRequest request) {
        return adminUserService.updateUser(id, request);
    }

    @Override
    public UserResponse deactivateUser(UUID id) {
        return adminUserService.deactivateUser(id);
    }
}
