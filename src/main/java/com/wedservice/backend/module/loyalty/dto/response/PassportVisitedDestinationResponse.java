package com.wedservice.backend.module.loyalty.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassportVisitedDestinationResponse {

    private Long visitedId;
    private Long destinationId;
    private UUID destinationUuid;
    private String destinationName;
    private String destinationSlug;
    private Long firstBookingId;
    private LocalDateTime firstVisitedAt;
    private LocalDateTime lastVisitedAt;
}
