package com.wedservice.backend.module.users.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.request.UserSearchRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.command.AdminUserCommandService;
import com.wedservice.backend.module.users.service.query.AdminUserQueryService;
import com.wedservice.backend.module.users.validator.AdminUserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminUserFacade {

    private final AdminUserCommandService commandService;
    private final AdminUserQueryService queryService;
    private final AdminUserValidator validator;

    public UserResponse createUser(AdminCreateUserRequest request) {
        String email = request.getEmail();
        String phone = request.getPhone();
        validator.validateRequiredContact(email, phone);
        validator.validateUniqueContacts(email, phone, null);
        return commandService.createUser(request);
    }

    public PageResponse<UserResponse> getUsers(UserSearchRequest request) {
        return queryService.getUsers(request);
    }

    public UserResponse getUserById(UUID id) {
        return queryService.getUserById(id);
    }

    public UserResponse updateUser(UUID id, AdminUpdateUserRequest request) {
        String email = request.getEmail();
        String phone = request.getPhone();
        validator.validateRequiredContact(email, phone);
        validator.validateUniqueContacts(email, phone, id);
        return commandService.updateUser(id, request);
    }

    public UserResponse deactivateUser(UUID id) {
        return commandService.deactivateUser(id);
    }
}
