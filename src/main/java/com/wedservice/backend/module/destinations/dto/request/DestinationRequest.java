package com.wedservice.backend.module.destinations.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 220, message = "Slug must not exceed 220 characters")
    private String slug;

    @Size(max = 2, message = "Country code must be 2 characters")
    private String countryCode;

    @NotBlank(message = "Province is required")
    @Size(max = 120, message = "Province must not exceed 120 characters")
    private String province;

    private String district;
    private String region;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String shortDescription;
    private String description;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer bestTimeFromMonth;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer bestTimeToMonth;

    private CrowdLevel crowdLevelDefault;
    private Boolean isFeatured;
    private Boolean isActive;
    private Boolean isOfficial;

    private List<@Valid DestinationMediaRequest> mediaList;
    private List<@Valid DestinationFoodRequest> foods;
    private List<@Valid DestinationSpecialtyRequest> specialties;
    private List<@Valid DestinationActivityRequest> activities;
    private List<@Valid DestinationTipRequest> tips;
    private List<@Valid DestinationEventRequest> events;
}
