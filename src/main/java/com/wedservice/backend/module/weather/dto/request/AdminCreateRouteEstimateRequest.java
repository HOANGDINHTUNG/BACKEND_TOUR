package com.wedservice.backend.module.weather.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateRouteEstimateRequest {

    @NotBlank
    @Size(max = 255)
    private String fromLabel;

    @NotBlank
    @Size(max = 255)
    private String toLabel;

    private BigDecimal fromLatitude;
    private BigDecimal fromLongitude;
    private BigDecimal toLatitude;
    private BigDecimal toLongitude;

    private BigDecimal distanceKm;

    @Min(0)
    private Integer durationMinutes;

    @Size(max = 2000)
    private String googleMapUrl;

    @Size(max = 100)
    private String sourceName;
}
