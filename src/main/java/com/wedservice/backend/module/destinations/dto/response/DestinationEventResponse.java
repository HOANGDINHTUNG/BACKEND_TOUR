package com.wedservice.backend.module.destinations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationEventResponse {
    private Long id;
    private String eventName;
    private String eventType;
    private String description;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Boolean notifyAllFollowers;
    private Boolean isActive;
}
