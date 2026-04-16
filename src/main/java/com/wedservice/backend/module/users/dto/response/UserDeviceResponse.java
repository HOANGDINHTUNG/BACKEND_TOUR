package com.wedservice.backend.module.users.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDeviceResponse {
    private Long id;
    private String platform;
    private String deviceName;
    private String pushToken;
    private String appVersion;
    private Boolean isActive;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
