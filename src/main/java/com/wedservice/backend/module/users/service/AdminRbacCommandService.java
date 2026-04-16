package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.users.dto.request.CreateRoleRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRolePermissionsRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRoleRequest;
import com.wedservice.backend.module.users.dto.response.PermissionResponse;
import com.wedservice.backend.module.users.dto.response.RoleResponse;
import com.wedservice.backend.module.users.entity.Permission;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.RoleScope;
import com.wedservice.backend.module.users.repository.PermissionRepository;
import com.wedservice.backend.module.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminRbacCommandService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditTrailRecorder auditTrailRecorder;

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedCode = normalizeRoleCode(request.getCode());
        if (roleRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new BadRequestException("Role code already exists");
        }

        RoleScope roleScope = request.getRoleScope();
        boolean isSystemRole = Boolean.TRUE.equals(request.getIsSystemRole());
        validateSystemRoleMutationAllowed(roleScope, isSystemRole, null);

        Role role = Role.builder()
                .code(normalizedCode)
                .name(normalizeRequiredText(request.getName(), "name"))
                .description(normalizeNullable(request.getDescription()))
                .roleScope(roleScope)
                .hierarchyLevel(request.getHierarchyLevel())
                .isSystemRole(isSystemRole)
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();

        Role savedRole = roleRepository.save(role);
        RoleResponse response = toRoleResponse(savedRole);
        auditTrailRecorder.record(AuditActionType.ROLE_CREATE, savedRole.getId(), null, response);
        return response;
    }

    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        RoleResponse oldState = toRoleResponse(role);

        String normalizedCode = normalizeRoleCode(request.getCode());
        if (roleRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new BadRequestException("Role code already exists");
        }

        validateSystemRoleMutationAllowed(request.getRoleScope(), request.getIsSystemRole(), role);

        role.setCode(normalizedCode);
        role.setName(normalizeRequiredText(request.getName(), "name"));
        role.setDescription(normalizeNullable(request.getDescription()));
        role.setRoleScope(request.getRoleScope());
        role.setHierarchyLevel(request.getHierarchyLevel());
        role.setIsSystemRole(request.getIsSystemRole());
        role.setIsActive(request.getIsActive());

        Role savedRole = roleRepository.save(role);
        RoleResponse response = toRoleResponse(savedRole);
        auditTrailRecorder.record(AuditActionType.ROLE_UPDATE, savedRole.getId(), oldState, response);
        return response;
    }

    @Transactional
    public RoleResponse updateRolePermissions(Long id, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        RoleResponse oldState = toRoleResponse(role);

        validateSystemRoleMutationAllowed(role.getRoleScope(), role.getIsSystemRole(), role);

        List<String> normalizedCodes = normalizePermissionCodes(request.getPermissionCodes());
        List<Permission> permissions = normalizedCodes.isEmpty()
                ? List.of()
                : permissionRepository.findAllByCodeIn(normalizedCodes);

        Set<String> foundCodes = permissions.stream()
                .map(Permission::getCode)
                .map(code -> code.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        List<String> missingCodes = normalizedCodes.stream()
                .filter(code -> !foundCodes.contains(code))
                .toList();
        if (!missingCodes.isEmpty()) {
            throw new BadRequestException("Permission code not found: " + String.join(", ", missingCodes));
        }

        List<String> inactiveCodes = permissions.stream()
                .filter(permission -> !Boolean.TRUE.equals(permission.getIsActive()))
                .map(Permission::getCode)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        if (!inactiveCodes.isEmpty()) {
            throw new BadRequestException("Inactive permissions cannot be assigned: " + String.join(", ", inactiveCodes));
        }

        role.setPermissions(new java.util.HashSet<>(permissions));
        Role savedRole = roleRepository.save(role);
        RoleResponse response = toRoleResponse(savedRole);
        auditTrailRecorder.record(AuditActionType.PERMISSION_ASSIGN, savedRole.getId(), oldState, response);
        return response;
    }

    private void validateSystemRoleMutationAllowed(RoleScope targetScope, Boolean targetIsSystemRole, Role existingRole) {
        boolean isSuperAdmin = authenticatedUserProvider.isCurrentUserInAnyRole("SUPER_ADMIN");
        boolean touchesSystemRole = Boolean.TRUE.equals(targetIsSystemRole)
                || targetScope == RoleScope.SYSTEM
                || (existingRole != null && Boolean.TRUE.equals(existingRole.getIsSystemRole()))
                || (existingRole != null && existingRole.getRoleScope() == RoleScope.SYSTEM);

        if (touchesSystemRole && !isSuperAdmin) {
            throw new BadRequestException("Only SUPER_ADMIN can create or modify system roles");
        }
    }

    private String normalizeRoleCode(String value) {
        String normalized = normalizeRequiredText(value, "code");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private List<String> normalizePermissionCodes(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String code = normalizeNullable(value);
            if (StringUtils.hasText(code)) {
                normalized.add(code.toLowerCase(Locale.ROOT));
            }
        }
        return List.copyOf(normalized);
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
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
