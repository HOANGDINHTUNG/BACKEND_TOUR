package com.wedservice.backend.module.destinations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationEventRequest {

    @NotBlank(message = "Event name is required")
    @Size(max = 200, message = "Event name must not exceed 200 characters")
    private String eventName;

    @Size(max = 80, message = "Event type must not exceed 80 characters")
    private String eventType;

    private String description;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Boolean notifyAllFollowers;
    private Boolean isActive;
}
