package com.wedservice.backend.module.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteEstimateResponse {

    private Long id;
    private String fromLabel;
    private String toLabel;
    private BigDecimal fromLatitude;
    private BigDecimal fromLongitude;
    private BigDecimal toLatitude;
    private BigDecimal toLongitude;
    private BigDecimal distanceKm;
    private Integer durationMinutes;
    private String googleMapUrl;
    private String sourceName;
    private LocalDateTime createdAt;
}
