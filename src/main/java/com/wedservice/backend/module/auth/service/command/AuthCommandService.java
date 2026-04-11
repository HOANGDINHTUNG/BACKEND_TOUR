package com.wedservice.backend.module.auth.service.command;

import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.RegisterRequest;

public interface AuthCommandService {
    AuthResponse register(RegisterRequest request);
}
