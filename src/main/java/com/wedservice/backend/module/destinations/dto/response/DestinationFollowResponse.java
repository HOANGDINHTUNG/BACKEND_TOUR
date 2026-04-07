package com.wedservice.backend.module.destinations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationFollowResponse {
    private Long id;
    private UUID destinationUuid;
    private String destinationName;
    private Boolean notifyEvent;
    private Boolean notifyVoucher;
    private Boolean notifyNewTour;
    private Boolean notifyBestSeason;
    private LocalDateTime createdAt;
}
