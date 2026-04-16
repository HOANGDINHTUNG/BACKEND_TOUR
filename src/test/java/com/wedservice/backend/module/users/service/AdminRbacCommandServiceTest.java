package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.users.dto.request.CreateRoleRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRolePermissionsRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRoleRequest;
import com.wedservice.backend.module.users.dto.response.RoleResponse;
import com.wedservice.backend.module.users.entity.Permission;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.RoleScope;
import com.wedservice.backend.module.users.repository.PermissionRepository;
import com.wedservice.backend.module.users.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRbacCommandServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    private AdminRbacCommandService adminRbacCommandService;

    @BeforeEach
    void setUp() {
        adminRbacCommandService = new AdminRbacCommandService(
                roleRepository,
                permissionRepository,
                authenticatedUserProvider,
                auditTrailRecorder
        );
    }

    @Test
    void createRole_createsCustomRoleWithNormalizedCode() {
        CreateRoleRequest request = CreateRoleRequest.builder()
                .code(" operator_custom ")
                .name(" Custom Operator ")
                .description(" Custom backoffice role ")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(50)
                .isSystemRole(false)
                .isActive(true)
                .build();

        when(roleRepository.findByCodeIgnoreCase("OPERATOR_CUSTOM")).thenReturn(Optional.empty());
        when(authenticatedUserProvider.isCurrentUserInAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(1L);
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            return role;
        });

        RoleResponse response = adminRbacCommandService.createRole(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("OPERATOR_CUSTOM");
        assertThat(response.getName()).isEqualTo("Custom Operator");
        assertThat(response.getDescription()).isEqualTo("Custom backoffice role");
        assertThat(response.getRoleScope()).isEqualTo(RoleScope.BACKOFFICE);
        verify(auditTrailRecorder).record(org.mockito.ArgumentMatchers.eq(AuditActionType.ROLE_CREATE), org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(RoleResponse.class));
    }

    @Test
    void createRole_rejectsSystemRoleForNonSuperAdmin() {
        CreateRoleRequest request = CreateRoleRequest.builder()
                .code("SYS_CUSTOM")
                .name("System Custom")
                .roleScope(RoleScope.SYSTEM)
                .hierarchyLevel(500)
                .isSystemRole(true)
                .isActive(true)
                .build();

        when(roleRepository.findByCodeIgnoreCase("SYS_CUSTOM")).thenReturn(Optional.empty());
        when(authenticatedUserProvider.isCurrentUserInAnyRole("SUPER_ADMIN")).thenReturn(false);

        assertThatThrownBy(() -> adminRbacCommandService.createRole(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only SUPER_ADMIN can create or modify system roles");
    }

    @Test
    void updateRole_updatesExistingRole() {
        Role existingRole = Role.builder()
                .id(5L)
                .code("EDITOR")
                .name("Editor")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(20)
                .isSystemRole(false)
                .isActive(true)
                .build();
        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .code("editor_plus")
                .name("Editor Plus")
                .description("Updated description")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(30)
                .isSystemRole(false)
                .isActive(false)
                .build();

        when(roleRepository.findWithPermissionsById(5L)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByCodeIgnoreCaseAndIdNot("EDITOR_PLUS", 5L)).thenReturn(false);
        when(authenticatedUserProvider.isCurrentUserInAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = adminRbacCommandService.updateRole(5L, request);

        assertThat(response.getCode()).isEqualTo("EDITOR_PLUS");
        assertThat(response.getName()).isEqualTo("Editor Plus");
        assertThat(response.getIsActive()).isFalse();
        verify(auditTrailRecorder).record(org.mockito.ArgumentMatchers.eq(AuditActionType.ROLE_UPDATE), org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.any(RoleResponse.class), org.mockito.ArgumentMatchers.any(RoleResponse.class));
    }

    @Test
    void updateRolePermissions_replacesPermissionsByCode() {
        Permission roleView = Permission.builder()
                .id(11L)
                .code("role.view")
                .name("View Role")
                .moduleName("role")
                .actionName("view")
                .isActive(true)
                .build();
        Permission permissionView = Permission.builder()
                .id(12L)
                .code("permission.view")
                .name("View Permission")
                .moduleName("permission")
                .actionName("view")
                .isActive(true)
                .build();
        Role existingRole = Role.builder()
                .id(8L)
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(40)
                .isSystemRole(false)
                .isActive(true)
                .permissions(Set.of())
                .build();
        UpdateRolePermissionsRequest request = UpdateRolePermissionsRequest.builder()
                .permissionCodes(List.of(" role.view ", "permission.view", "role.view"))
                .build();

        when(roleRepository.findWithPermissionsById(8L)).thenReturn(Optional.of(existingRole));
        when(authenticatedUserProvider.isCurrentUserInAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(permissionRepository.findAllByCodeIn(List.of("role.view", "permission.view")))
                .thenReturn(List.of(roleView, permissionView));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = adminRbacCommandService.updateRolePermissions(8L, request);

        assertThat(response.getPermissions()).hasSize(2);
        verify(permissionRepository).findAllByCodeIn(List.of("role.view", "permission.view"));
        verify(auditTrailRecorder).record(org.mockito.ArgumentMatchers.eq(AuditActionType.PERMISSION_ASSIGN), org.mockito.ArgumentMatchers.eq(8L), org.mockito.ArgumentMatchers.any(RoleResponse.class), org.mockito.ArgumentMatchers.any(RoleResponse.class));
    }

    @Test
    void updateRolePermissions_rejectsMissingPermissionCodes() {
        Role existingRole = Role.builder()
                .id(9L)
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(40)
                .isSystemRole(false)
                .isActive(true)
                .permissions(Set.of())
                .build();
        UpdateRolePermissionsRequest request = UpdateRolePermissionsRequest.builder()
                .permissionCodes(List.of("role.view", "permission.view"))
                .build();

        when(roleRepository.findWithPermissionsById(9L)).thenReturn(Optional.of(existingRole));
        when(authenticatedUserProvider.isCurrentUserInAnyRole("SUPER_ADMIN")).thenReturn(false);
        when(permissionRepository.findAllByCodeIn(List.of("role.view", "permission.view")))
                .thenReturn(List.of(
                        Permission.builder()
                                .id(11L)
                                .code("role.view")
                                .name("View Role")
                                .moduleName("role")
                                .actionName("view")
                                .isActive(true)
                                .build()
                ));

        assertThatThrownBy(() -> adminRbacCommandService.updateRolePermissions(9L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Permission code not found: permission.view");
    }
}
