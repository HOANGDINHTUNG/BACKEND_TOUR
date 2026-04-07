package com.wedservice.backend.module.destinations.dto.response;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationResponse {
    private UUID uuid;
    private String code;
    private String name;
    private String slug;
    private String countryCode;
    private String province;
    private String district;
    private String region;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String shortDescription;
    private String description;
    private Integer bestTimeFromMonth;
    private Integer bestTimeToMonth;
    private CrowdLevel crowdLevelDefault;
    private Boolean isFeatured;
    private Boolean isActive;
    private DestinationStatus status;
    private UUID proposedBy;
    private UUID verifiedBy;
    private String rejectionReason;
    private Boolean isOfficial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
