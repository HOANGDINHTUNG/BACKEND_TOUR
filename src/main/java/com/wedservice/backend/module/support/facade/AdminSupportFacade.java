package com.wedservice.backend.module.support.facade;

import com.wedservice.backend.module.support.dto.request.AssignSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.UpdateSupportSessionStatusRequest;
import com.wedservice.backend.module.support.dto.response.SupportMessageResponse;
import com.wedservice.backend.module.support.dto.response.SupportSessionResponse;
import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import com.wedservice.backend.module.support.service.AdminSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminSupportFacade {

    private final AdminSupportService adminSupportService;

    public List<SupportSessionResponse> getSupportSessions(SupportSessionStatus status, UUID userId, UUID assignedStaffId) {
        return adminSupportService.getSupportSessions(status, userId, assignedStaffId);
    }

    public SupportSessionResponse getSupportSession(Long id) {
        return adminSupportService.getSupportSession(id);
    }

    public SupportSessionResponse assignSupportSession(Long id, AssignSupportSessionRequest request) {
        return adminSupportService.assignSupportSession(id, request);
    }

    public SupportSessionResponse updateSupportSessionStatus(Long id, UpdateSupportSessionStatusRequest request) {
        return adminSupportService.updateSupportSessionStatus(id, request);
    }

    public SupportMessageResponse sendSupportReply(Long sessionId, CreateSupportMessageRequest request) {
        return adminSupportService.sendSupportReply(sessionId, request);
    }
}
