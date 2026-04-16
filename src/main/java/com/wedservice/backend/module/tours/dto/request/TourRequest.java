package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourRequest {
    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 280, message = "Slug must not exceed 280 characters")
    private String slug;

    @NotNull(message = "Destination id is required")
    private Long destinationId;

    private Long cancellationPolicyId;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be greater than or equal to 0")
    private BigDecimal basePrice;

    @Size(min = 3, max = 3, message = "Currency must be a 3-character code")
    private String currency;

    @NotNull(message = "Duration days is required")
    @Min(value = 1, message = "Duration days must be at least 1")
    private Integer durationDays;

    @Min(value = 0, message = "Duration nights must be greater than or equal to 0")
    private Integer durationNights;

    @Size(max = 120, message = "Transport type must not exceed 120 characters")
    private String transportType;

    @Size(max = 50, message = "Trip mode must not exceed 50 characters")
    private String tripMode;

    private String shortDescription;
    private String description;
    private String highlights;
    private String inclusions;
    private String exclusions;
    private String notes;
    private Boolean isFeatured;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private List<Long> tagIds;
    private List<@Valid TourMediaRequest> media;
    private List<@Valid TourSeasonalityRequest> seasonality;
    private List<@Valid TourItineraryDayRequest> itineraryDays;
    private List<@Valid TourChecklistItemRequest> checklistItems;
}
