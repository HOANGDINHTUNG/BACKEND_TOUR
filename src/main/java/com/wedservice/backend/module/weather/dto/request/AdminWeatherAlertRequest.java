package com.wedservice.backend.module.weather.dto.request;

import com.wedservice.backend.module.weather.entity.WeatherSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AdminWeatherAlertRequest {

    private Long scheduleId;

    @NotNull
    private WeatherSeverity severity;

    @NotBlank
    @Size(max = 100)
    private String alertType;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String message;

    private String actionAdvice;

    @NotNull
    private LocalDateTime validFrom;

    @NotNull
    private LocalDateTime validTo;

    private Boolean isActive;
}
