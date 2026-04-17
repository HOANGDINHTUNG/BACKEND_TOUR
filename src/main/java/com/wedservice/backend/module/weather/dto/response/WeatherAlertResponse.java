package com.wedservice.backend.module.weather.dto.response;

import com.wedservice.backend.module.weather.entity.WeatherSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherAlertResponse {

    private Long id;
    private Long destinationId;
    private Long scheduleId;
    private WeatherSeverity severity;
    private String alertType;
    private String title;
    private String message;
    private String actionAdvice;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
