package com.wedservice.backend.module.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherForecastResponse {

    private Long id;
    private Long destinationId;
    private LocalDate forecastDate;
    private String weatherCode;
    private String summary;
    private BigDecimal tempMin;
    private BigDecimal tempMax;
    private BigDecimal humidityPercent;
    private BigDecimal windSpeed;
    private BigDecimal rainProbability;
    private String sourceName;
    private String rawPayload;
    private LocalDateTime createdAt;
}
