package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.users.dto.response.PermissionResponse;
import com.wedservice.backend.module.users.dto.response.RoleResponse;
import com.wedservice.backend.module.users.entity.Permission;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.repository.PermissionRepository;
import com.wedservice.backend.module.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRbacQueryService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAllByOrderByHierarchyLevelDescNameAsc()
                .stream()
                .map(this::toRoleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        return roleRepository.findWithPermissionsById(id)
                .map(this::toRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions() {
        return permissionRepository.findAllByOrderByModuleNameAscActionNameAscNameAsc()
                .stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    private RoleResponse toRoleResponse(Role role) {
        List<PermissionResponse> permissions = role.getPermissions().stream()
                .sorted(Comparator
                        .comparing(Permission::getModuleName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Permission::getActionName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Permission::getCode, String.CASE_INSENSITIVE_ORDER))
                .map(this::toPermissionResponse)
                .toList();

        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .roleScope(role.getRoleScope())
                .hierarchyLevel(role.getHierarchyLevel())
                .isSystemRole(role.getIsSystemRole())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .permissions(permissions)
                .build();
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .moduleName(permission.getModuleName())
                .actionName(permission.getActionName())
                .description(permission.getDescription())
                .isActive(permission.getIsActive())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
