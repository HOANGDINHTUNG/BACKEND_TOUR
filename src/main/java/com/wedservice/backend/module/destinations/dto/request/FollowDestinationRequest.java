package com.wedservice.backend.module.destinations.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowDestinationRequest {
    private Boolean notifyEvent;
    private Boolean notifyVoucher;
    private Boolean notifyNewTour;
    private Boolean notifyBestSeason;
}
