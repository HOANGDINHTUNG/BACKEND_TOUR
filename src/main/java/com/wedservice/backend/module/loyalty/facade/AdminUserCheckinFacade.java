package com.wedservice.backend.module.loyalty.facade;

import com.wedservice.backend.module.loyalty.dto.request.CreateUserCheckinRequest;
import com.wedservice.backend.module.loyalty.dto.response.UserCheckinResponse;
import com.wedservice.backend.module.loyalty.service.UserPassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminUserCheckinFacade {

    private final UserPassportService userPassportService;

    public UserCheckinResponse createCheckin(UUID userId, CreateUserCheckinRequest request) {
        return userPassportService.createCheckinForUser(userId, request, true);
    }
}
