package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.users.dto.response.PermissionResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRbacQueryServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    private AdminRbacQueryService adminRbacQueryService;

    @BeforeEach
    void setUp() {
        adminRbacQueryService = new AdminRbacQueryService(roleRepository, permissionRepository);
    }

    @Test
    void getRoles_returnsRolesWithSortedPermissions() {
        LocalDateTime now = LocalDateTime.now();
        Permission userUpdate = Permission.builder()
                .id(2L)
                .code("user.update")
                .name("Update User")
                .moduleName("user")
                .actionName("update")
                .isActive(true)
                .createdAt(now)
                .build();
        Permission userView = Permission.builder()
                .id(1L)
                .code("user.view")
                .name("View User")
                .moduleName("user")
                .actionName("view")
                .isActive(true)
                .createdAt(now)
                .build();
        Role adminRole = Role.builder()
                .id(11L)
                .code("ADMIN")
                .name("Admin")
                .description("System admin")
                .roleScope(RoleScope.SYSTEM)
                .hierarchyLevel(100)
                .isSystemRole(true)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .permissions(Set.of(userUpdate, userView))
                .build();

        when(roleRepository.findAllByOrderByHierarchyLevelDescNameAsc()).thenReturn(List.of(adminRole));

        List<RoleResponse> responses = adminRbacQueryService.getRoles();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCode()).isEqualTo("ADMIN");
        assertThat(responses.get(0).getPermissions()).hasSize(2);
        assertThat(responses.get(0).getPermissions().get(0).getCode()).isEqualTo("user.update");
        assertThat(responses.get(0).getPermissions().get(1).getCode()).isEqualTo("user.view");
    }

    @Test
    void getRoleById_throwsWhenRoleDoesNotExist() {
        when(roleRepository.findWithPermissionsById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminRbacQueryService.getRoleById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found with id: 99");
    }

    @Test
    void getPermissions_returnsSortedPermissionResponses() {
        LocalDateTime now = LocalDateTime.now();
        Permission permission = Permission.builder()
                .id(21L)
                .code("booking.view")
                .name("View Booking")
                .moduleName("booking")
                .actionName("view")
                .description("Allow viewing bookings")
                .isActive(true)
                .createdAt(now)
                .build();

        when(permissionRepository.findAllByOrderByModuleNameAscActionNameAscNameAsc()).thenReturn(List.of(permission));

        List<PermissionResponse> responses = adminRbacQueryService.getPermissions();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCode()).isEqualTo("booking.view");
        assertThat(responses.get(0).getModuleName()).isEqualTo("booking");
    }
}
