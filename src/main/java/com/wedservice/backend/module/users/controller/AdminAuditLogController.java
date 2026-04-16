package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AuditLogSearchRequest;
import com.wedservice.backend.module.users.dto.response.AuditLogResponse;
import com.wedservice.backend.module.users.facade.AdminAuditLogFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogFacade adminAuditLogFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('audit.view')")
    public ApiResponse<PageResponse<AuditLogResponse>> getAuditLogs(@Valid @ModelAttribute AuditLogSearchRequest request) {
        return ApiResponse.<PageResponse<AuditLogResponse>>builder()
                .success(true)
                .message("Audit log list fetched successfully")
                .data(adminAuditLogFacade.getAuditLogs(request))
                .build();
    }
}
