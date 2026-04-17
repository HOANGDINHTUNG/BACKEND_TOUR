package com.wedservice.backend.module.support.facade;

import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.RateSupportSessionRequest;
import com.wedservice.backend.module.support.dto.response.SupportMessageResponse;
import com.wedservice.backend.module.support.dto.response.SupportSessionResponse;
import com.wedservice.backend.module.support.service.UserSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSupportFacade {

    private final UserSupportService userSupportService;

    public SupportSessionResponse createMySupportSession(CreateSupportSessionRequest request) {
        return userSupportService.createMySupportSession(request);
    }

    public List<SupportSessionResponse> getMySupportSessions() {
        return userSupportService.getMySupportSessions();
    }

    public SupportSessionResponse getMySupportSession(Long id) {
        return userSupportService.getMySupportSession(id);
    }

    public SupportMessageResponse sendMySupportMessage(Long sessionId, CreateSupportMessageRequest request) {
        return userSupportService.sendMySupportMessage(sessionId, request);
    }

    public SupportSessionResponse rateMySupportSession(Long sessionId, com.wedservice.backend.module.support.dto.request.RateSupportSessionRequest request) {
        return userSupportService.rateMySupportSession(sessionId, request);
    }
}
