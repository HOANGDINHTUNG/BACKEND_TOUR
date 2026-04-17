package com.wedservice.backend.module.weather.dto.request;

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
public class AdminUpsertWeatherForecastRequest {

    @Size(max = 50)
    private String weatherCode;

    @Size(max = 255)
    private String summary;

    private BigDecimal tempMin;

    private BigDecimal tempMax;

    private BigDecimal humidityPercent;

    private BigDecimal windSpeed;

    private BigDecimal rainProbability;

    @Size(max = 100)
    private String sourceName;

    private String rawPayload;
}
