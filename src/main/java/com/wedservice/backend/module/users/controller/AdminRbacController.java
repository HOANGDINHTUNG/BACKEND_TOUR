package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.users.dto.request.CreateRoleRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRolePermissionsRequest;
import com.wedservice.backend.module.users.dto.request.UpdateRoleRequest;
import com.wedservice.backend.module.users.dto.response.PermissionResponse;
import com.wedservice.backend.module.users.dto.response.RoleResponse;
import com.wedservice.backend.module.users.facade.AdminRbacFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AdminRbacController {

    private final AdminRbacFacade adminRbacFacade;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.<List<RoleResponse>>builder()
                .success(true)
                .message("Role list fetched successfully")
                .data(adminRbacFacade.getRoles())
                .build();
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<RoleResponse> getRoleById(@PathVariable Long id) {
        return ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role fetched successfully")
                .data(adminRbacFacade.getRoleById(id))
                .build();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<List<PermissionResponse>> getPermissions() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permission list fetched successfully")
                .data(adminRbacFacade.getPermissions())
                .build();
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('role.assign')")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role created successfully")
                .data(adminRbacFacade.createRole(request))
                .build();
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('role.assign')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role updated successfully")
                .data(adminRbacFacade.updateRole(id, request))
                .build();
    }

    @PatchMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('role.assign')")
    public ApiResponse<RoleResponse> updateRolePermissions(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role permissions updated successfully")
                .data(adminRbacFacade.updateRolePermissions(id, request))
                .build();
    }
}
