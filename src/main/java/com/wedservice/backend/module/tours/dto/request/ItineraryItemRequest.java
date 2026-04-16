package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryItemRequest {

    @NotNull
    @Min(1)
    private Integer sequenceNo;

    @NotBlank
    @Size(max = 50)
    private String itemType;

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;
    private Long destinationId;

    @Size(max = 255)
    private String locationName;

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String googleMapUrl;
    private LocalTime startTime;
    private LocalTime endTime;

    @Min(0)
    private Integer travelMinutesEstimated;
}
