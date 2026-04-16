package com.wedservice.backend.module.users.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AuditLogSearchRequest;
import com.wedservice.backend.module.users.dto.response.AuditLogResponse;
import com.wedservice.backend.module.users.service.AdminAuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuditLogFacade {

    private final AdminAuditLogQueryService adminAuditLogQueryService;

    public PageResponse<AuditLogResponse> getAuditLogs(AuditLogSearchRequest request) {
        return adminAuditLogQueryService.getAuditLogs(request);
    }
}
