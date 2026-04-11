package com.wedservice.backend.module.auth.facade;

import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.LoginRequest;
import com.wedservice.backend.module.auth.dto.RegisterRequest;
import com.wedservice.backend.module.auth.service.command.AuthCommandService;
import com.wedservice.backend.module.auth.service.query.AuthQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;

    public AuthResponse register(RegisterRequest request) {
        // authValidator.validateRequiredContact and uniqueness already applied inside command service,
        // but we can call validator here if orchestration needs it earlier.
        return authCommandService.register(request);
    }

    public AuthResponse login(LoginRequest request) {
        return authQueryService.login(request);
    }
}
