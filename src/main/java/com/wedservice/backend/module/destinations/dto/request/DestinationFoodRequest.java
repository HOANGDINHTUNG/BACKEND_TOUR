package com.wedservice.backend.module.destinations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationFoodRequest {

    @NotBlank(message = "Food name is required")
    @Size(max = 200, message = "Food name must not exceed 200 characters")
    private String foodName;

    private String description;
    private Boolean isFeatured;
}
