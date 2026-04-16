package com.wedservice.backend.module.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceRequest {

    @NotBlank(message = "platform is required")
    @Size(max = 30, message = "platform must not exceed 30 characters")
    private String platform;

    @Size(max = 100, message = "deviceName must not exceed 100 characters")
    private String deviceName;

    private String pushToken;

    @Size(max = 30, message = "appVersion must not exceed 30 characters")
    private String appVersion;
}
