package com.wedservice.backend.module.tours.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourResponse {
    private Long id;
    private String code;
    private String name;
    private String slug;
    private Long destinationId;
    private Long cancellationPolicyId;
    private BigDecimal basePrice;
    private String currency;
    private Integer durationDays;
    private Integer durationNights;
    private String shortDescription;
    private String description;
    private String transportType;
    private String tripMode;
    private String highlights;
    private String inclusions;
    private String exclusions;
    private String notes;
    private Boolean isFeatured;
    private String status;
    private List<TagResponse> tags;
    private List<TourMediaResponse> media;
    private List<TourSeasonalityResponse> seasonality;
    private List<TourItineraryDayResponse> itineraryDays;
    private List<TourChecklistItemResponse> checklistItems;
    private CancellationPolicyResponse cancellationPolicy;
}
