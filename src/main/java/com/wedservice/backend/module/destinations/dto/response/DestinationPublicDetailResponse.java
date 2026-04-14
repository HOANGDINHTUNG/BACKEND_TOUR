package com.wedservice.backend.module.destinations.dto.response;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationPublicDetailResponse {
    private UUID uuid;
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
    private List<DestinationMediaResponse> mediaList;
    private List<DestinationFoodResponse> foods;
    private List<DestinationSpecialtyResponse> specialties;
    private List<DestinationActivityResponse> activities;
    private List<DestinationTipResponse> tips;
    private List<DestinationEventResponse> events;
}
