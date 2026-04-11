package com.wedservice.backend.module.users.service.command;

import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;

import java.util.UUID;

public interface AdminUserCommandService {
    UserResponse createUser(AdminCreateUserRequest request);
    UserResponse updateUser(UUID id, AdminUpdateUserRequest request);
    UserResponse deactivateUser(UUID id);
}
