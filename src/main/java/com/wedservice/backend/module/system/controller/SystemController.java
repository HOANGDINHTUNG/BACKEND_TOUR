package com.wedservice.backend.module.system.controller;

import com.wedservice.backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class SystemController {

    @GetMapping("/system/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Application is running")
                .data(Map.of(
                        "service", "wedservice-backend",
                        "status", "OK",
                        "time", LocalDateTime.now()
                ))
                .build();
    }
}
