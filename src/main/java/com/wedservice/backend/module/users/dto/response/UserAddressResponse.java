package com.wedservice.backend.module.users.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserAddressResponse {
    private Long id;
    private String contactName;
    private String contactPhone;
    private String province;
    private String district;
    private String ward;
    private String addressLine;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
