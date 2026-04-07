package com.wedservice.backend.module.destinations.dto.request;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationSearchRequest {
    private String keyword;
    private String province;
    private String region;
    private CrowdLevel crowdLevel;
    private Boolean isFeatured;
    private Boolean isActive;
    private Boolean isOfficial;
    private com.wedservice.backend.module.destinations.entity.DestinationStatus status;

    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
    @Builder.Default
    private String sortBy = "name";
    @Builder.Default
    private String sortDir = "asc";
}
