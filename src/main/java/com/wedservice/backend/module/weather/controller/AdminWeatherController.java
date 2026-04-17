package com.wedservice.backend.module.weather.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertCrowdPredictionRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertWeatherForecastRequest;
import com.wedservice.backend.module.weather.dto.request.AdminWeatherAlertRequest;
import com.wedservice.backend.module.weather.dto.request.UpdateWeatherAlertStatusRequest;
import com.wedservice.backend.module.weather.dto.response.CrowdPredictionResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherAlertResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherForecastResponse;
import com.wedservice.backend.module.weather.facade.AdminWeatherFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/destinations/{destinationUuid}/weather")
@RequiredArgsConstructor
public class AdminWeatherController {

    private final AdminWeatherFacade adminWeatherFacade;

    @GetMapping("/forecasts")
    @PreAuthorize("hasAuthority('destination.view')")
    public ApiResponse<List<WeatherForecastResponse>> getForecasts(@PathVariable UUID destinationUuid) {
        return ApiResponse.<List<WeatherForecastResponse>>builder()
                .success(true)
                .message("Weather forecasts fetched successfully")
                .data(adminWeatherFacade.getForecasts(destinationUuid))
                .build();
    }

    @PutMapping("/forecasts/{forecastDate}")
    @PreAuthorize("hasAuthority('destination.update')")
    public ApiResponse<WeatherForecastResponse> upsertForecast(
            @PathVariable UUID destinationUuid,
            @PathVariable LocalDate forecastDate,
            @Valid @RequestBody AdminUpsertWeatherForecastRequest request
    ) {
        return ApiResponse.<WeatherForecastResponse>builder()
                .success(true)
                .message("Weather forecast saved successfully")
                .data(adminWeatherFacade.upsertForecast(destinationUuid, forecastDate, request))
                .build();
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('destination.view')")
    public ApiResponse<List<WeatherAlertResponse>> getAlerts(@PathVariable UUID destinationUuid) {
        return ApiResponse.<List<WeatherAlertResponse>>builder()
                .success(true)
                .message("Weather alerts fetched successfully")
                .data(adminWeatherFacade.getAlerts(destinationUuid))
                .build();
    }

    @PostMapping("/alerts")
    @PreAuthorize("hasAuthority('destination.update')")
    public ApiResponse<WeatherAlertResponse> createAlert(
            @PathVariable UUID destinationUuid,
            @Valid @RequestBody AdminWeatherAlertRequest request
    ) {
        return ApiResponse.<WeatherAlertResponse>builder()
                .success(true)
                .message("Weather alert created successfully")
                .data(adminWeatherFacade.createAlert(destinationUuid, request))
                .build();
    }

    @PutMapping("/alerts/{alertId}")
    @PreAuthorize("hasAuthority('destination.update')")
    public ApiResponse<WeatherAlertResponse> updateAlert(
            @PathVariable UUID destinationUuid,
            @PathVariable Long alertId,
            @Valid @RequestBody AdminWeatherAlertRequest request
    ) {
        return ApiResponse.<WeatherAlertResponse>builder()
                .success(true)
                .message("Weather alert updated successfully")
                .data(adminWeatherFacade.updateAlert(destinationUuid, alertId, request))
                .build();
    }

    @PatchMapping("/alerts/{alertId}/status")
    @PreAuthorize("hasAuthority('destination.update')")
    public ApiResponse<WeatherAlertResponse> updateAlertStatus(
            @PathVariable UUID destinationUuid,
            @PathVariable Long alertId,
            @Valid @RequestBody UpdateWeatherAlertStatusRequest request
    ) {
        return ApiResponse.<WeatherAlertResponse>builder()
                .success(true)
                .message("Weather alert status updated successfully")
                .data(adminWeatherFacade.updateAlertStatus(destinationUuid, alertId, request))
                .build();
    }

    @GetMapping("/crowd-predictions")
    @PreAuthorize("hasAuthority('destination.view')")
    public ApiResponse<List<CrowdPredictionResponse>> getCrowdPredictions(@PathVariable UUID destinationUuid) {
        return ApiResponse.<List<CrowdPredictionResponse>>builder()
                .success(true)
                .message("Crowd predictions fetched successfully")
                .data(adminWeatherFacade.getCrowdPredictions(destinationUuid))
                .build();
    }

    @PutMapping("/crowd-predictions/{predictionDate}")
    @PreAuthorize("hasAuthority('destination.update')")
    public ApiResponse<CrowdPredictionResponse> upsertCrowdPrediction(
            @PathVariable UUID destinationUuid,
            @PathVariable LocalDate predictionDate,
            @Valid @RequestBody AdminUpsertCrowdPredictionRequest request
    ) {
        return ApiResponse.<CrowdPredictionResponse>builder()
                .success(true)
                .message("Crowd prediction saved successfully")
                .data(adminWeatherFacade.upsertCrowdPrediction(destinationUuid, predictionDate, request))
                .build();
    }
}
