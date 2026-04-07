package com.wedservice.backend.module.destinations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationActivityRequest {

    @NotBlank(message = "Activity name is required")
    @Size(max = 200, message = "Activity name must not exceed 200 characters")
    private String activityName;

    private String description;
    private BigDecimal activityScore;
}
