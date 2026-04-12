package com.wedservice.backend.common.controller;

import com.wedservice.backend.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simple public health-check endpoint.
 * Used by DevOps, load balancers, and quick post-deploy verification.
 *
 * <p>Endpoint: {@code GET /system/health} — PUBLIC, no authentication required.</p>
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Value("${spring.application.name:wedservice-backend}")
    private String applicationName;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = Map.of(
                "service", applicationName,
                "status", "OK",
                "time", LocalDateTime.now().toString()
        );
        return ApiResponse.success(data, "Application is running");
    }
}
