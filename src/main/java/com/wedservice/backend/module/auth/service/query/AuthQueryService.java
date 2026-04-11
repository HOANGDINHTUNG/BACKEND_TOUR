package com.wedservice.backend.module.auth.service.query;

import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.LoginRequest;

public interface AuthQueryService {
    AuthResponse login(LoginRequest request);
}
