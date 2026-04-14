package com.wedservice.backend.module.destinations.dto.response;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationPublicResponse {
    private UUID uuid;
    private String name;
    private String slug;
    private String countryCode;
    private String province;
    private String district;
    private String region;
    private String shortDescription;
    private Integer bestTimeFromMonth;
    private Integer bestTimeToMonth;
    private CrowdLevel crowdLevelDefault;
    private Boolean isFeatured;
    private String coverImageUrl;
}
