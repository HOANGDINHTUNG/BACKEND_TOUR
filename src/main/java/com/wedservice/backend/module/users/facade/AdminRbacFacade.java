package com.wedservice.backend.module.users.facade;

import com.wedservice.backend.module.users.dto.request.CreateRoleRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRolePermissionsRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRoleRequest;
import com.wedservice.backend.module.users.dto.response.PermissionResponse;
import com.wedservice.backend.module.users.dto.response.RoleResponse;
import com.wedservice.backend.module.users.service.AdminRbacCommandService;
import com.wedservice.backend.module.users.service.AdminRbacQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminRbacFacade {

    private final AdminRbacQueryService adminRbacQueryService;
    private final AdminRbacCommandService adminRbacCommandService;

    public List<RoleResponse> getRoles() {
        return adminRbacQueryService.getRoles();
    }

    public RoleResponse getRoleById(Long id) {
        return adminRbacQueryService.getRoleById(id);
    }

    public List<PermissionResponse> getPermissions() {
        return adminRbacQueryService.getPermissions();
    }

    public RoleResponse createRole(CreateRoleRequest request) {
        return adminRbacCommandService.createRole(request);
    }

    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        return adminRbacCommandService.updateRole(id, request);
    }

    public RoleResponse updateRolePermissions(Long id, UpdateRolePermissionsRequest request) {
        return adminRbacCommandService.updateRolePermissions(id, request);
    }
}
