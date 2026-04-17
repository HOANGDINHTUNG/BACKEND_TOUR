package com.wedservice.backend.module.loyalty.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPassportResponse {

    private Long passportId;
    private UUID userId;
    private String passportNo;
    private Integer totalTours;
    private Integer totalDestinations;
    private Integer totalCheckins;
    private LocalDateTime lastTripAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PassportBadgeResponse> badges;
    private List<PassportVisitedDestinationResponse> visitedDestinations;
}
