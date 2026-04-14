package com.wedservice.backend.common.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.system.facade.SystemFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple public health-check endpoint.
 * Used by DevOps, load balancers, and quick post-deploy verification.
 *
 * <p>Endpoint: {@code GET /system/health} — PUBLIC, no authentication required.</p>
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    @Value("${spring.application.name:wedservice-backend}")
    private String applicationName;

    private final SystemFacade systemFacade;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = systemFacade.health(applicationName);
        return ApiResponse.success(data, "Application is running");
    }
}
