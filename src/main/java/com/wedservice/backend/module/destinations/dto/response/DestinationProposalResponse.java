package com.wedservice.backend.module.destinations.dto.response;

import com.wedservice.backend.module.destinations.entity.DestinationStatus;
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
public class DestinationProposalResponse {
    private UUID uuid;
    private String name;
    private String province;
    private String district;
    private String region;
    private DestinationStatus status;
    private LocalDateTime submittedAt;
}
