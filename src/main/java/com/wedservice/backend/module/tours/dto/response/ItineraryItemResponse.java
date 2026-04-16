package com.wedservice.backend.module.tours.dto.response;

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
public class ItineraryItemResponse {

    private Long id;
    private Integer sequenceNo;
    private String itemType;
    private String title;
    private String description;
    private Long destinationId;
    private String locationName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String googleMapUrl;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer travelMinutesEstimated;
}
