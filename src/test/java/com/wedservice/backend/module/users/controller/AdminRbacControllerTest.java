package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.users.dto.request.CreateRoleRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRolePermissionsRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRoleRequest;
import com.wedservice.backend.module.users.dto.response.PermissionResponse;
import com.wedservice.backend.module.users.dto.response.RoleResponse;
import com.wedservice.backend.module.users.entity.RoleScope;
import com.wedservice.backend.module.users.facade.AdminRbacFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminRbacControllerTest {

    @Mock
    private AdminRbacFacade adminRbacFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminRbacController(adminRbacFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getRoles_returnsWrappedApiResponse() throws Exception {
        RoleResponse response = RoleResponse.builder()
                .id(1L)
                .code("ADMIN")
                .name("Admin")
                .roleScope(RoleScope.SYSTEM)
                .isSystemRole(true)
                .isActive(true)
                .permissions(List.of(
                        PermissionResponse.builder()
                                .id(10L)
                                .code("user.view")
                                .moduleName("user")
                                .actionName("view")
                                .name("View User")
                                .isActive(true)
                                .build()
                ))
                .build();

        when(adminRbacFacade.getRoles()).thenReturn(List.of(response));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role list fetched successfully"))
                .andExpect(jsonPath("$.data[0].code").value("ADMIN"));
    }

    @Test
    void getRoleById_returnsWrappedApiResponse() throws Exception {
        RoleResponse response = RoleResponse.builder()
                .id(2L)
                .code("OPERATOR")
                .name("Operator")
                .roleScope(RoleScope.SYSTEM)
                .isSystemRole(true)
                .isActive(true)
                .permissions(List.of())
                .build();

        when(adminRbacFacade.getRoleById(2L)).thenReturn(response);

        mockMvc.perform(get("/roles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role fetched successfully"))
                .andExpect(jsonPath("$.data.code").value("OPERATOR"));
    }

    @Test
    void getPermissions_returnsWrappedApiResponse() throws Exception {
        PermissionResponse response = PermissionResponse.builder()
                .id(3L)
                .code("booking.view")
                .name("View Booking")
                .moduleName("booking")
                .actionName("view")
                .isActive(true)
                .build();

        when(adminRbacFacade.getPermissions()).thenReturn(List.of(response));

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Permission list fetched successfully"))
                .andExpect(jsonPath("$.data[0].code").value("booking.view"));
    }

    @Test
    void createRole_returnsWrappedApiResponse() throws Exception {
        CreateRoleRequest request = CreateRoleRequest.builder()
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .description("Can audit RBAC")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(40)
                .isSystemRole(false)
                .isActive(true)
                .build();
        RoleResponse response = RoleResponse.builder()
                .id(20L)
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .roleScope(RoleScope.BACKOFFICE)
                .isSystemRole(false)
                .isActive(true)
                .permissions(List.of())
                .build();

        when(adminRbacFacade.createRole(org.mockito.ArgumentMatchers.any(CreateRoleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role created successfully"))
                .andExpect(jsonPath("$.data.code").value("RBAC_AUDITOR"));
    }

    @Test
    void updateRole_returnsWrappedApiResponse() throws Exception {
        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .description("Updated")
                .roleScope(RoleScope.BACKOFFICE)
                .hierarchyLevel(45)
                .isSystemRole(false)
                .isActive(true)
                .build();
        RoleResponse response = RoleResponse.builder()
                .id(20L)
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .roleScope(RoleScope.BACKOFFICE)
                .isSystemRole(false)
                .isActive(true)
                .permissions(List.of())
                .build();

        when(adminRbacFacade.updateRole(org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.any(UpdateRoleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/roles/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role updated successfully"))
                .andExpect(jsonPath("$.data.name").value("RBAC Auditor"));
    }

    @Test
    void updateRolePermissions_returnsWrappedApiResponse() throws Exception {
        UpdateRolePermissionsRequest request = UpdateRolePermissionsRequest.builder()
                .permissionCodes(List.of("role.view", "permission.view"))
                .build();
        RoleResponse response = RoleResponse.builder()
                .id(20L)
                .code("RBAC_AUDITOR")
                .name("RBAC Auditor")
                .roleScope(RoleScope.BACKOFFICE)
                .isSystemRole(false)
                .isActive(true)
                .permissions(List.of(
                        PermissionResponse.builder()
                                .id(30L)
                                .code("role.view")
                                .name("View Role")
                                .moduleName("role")
                                .actionName("view")
                                .isActive(true)
                                .build()
                ))
                .build();

        when(adminRbacFacade.updateRolePermissions(org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.any(UpdateRolePermissionsRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/roles/20/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role permissions updated successfully"))
                .andExpect(jsonPath("$.data.permissions[0].code").value("role.view"));
    }
}
