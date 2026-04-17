package com.wedservice.backend.module.weather.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.weather.dto.response.CrowdPredictionResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherAlertResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherForecastResponse;
import com.wedservice.backend.module.weather.facade.WeatherFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/destinations/{destinationUuid}/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherFacade weatherFacade;

    @GetMapping("/forecasts")
    public ApiResponse<List<WeatherForecastResponse>> getDestinationForecasts(@PathVariable UUID destinationUuid) {
        return ApiResponse.<List<WeatherForecastResponse>>builder()
                .success(true)
                .message("Weather forecasts fetched successfully")
                .data(weatherFacade.getDestinationForecasts(destinationUuid))
                .build();
    }

    @GetMapping("/alerts")
    public ApiResponse<List<WeatherAlertResponse>> getDestinationAlerts(@PathVariable UUID destinationUuid) {
        return ApiResponse.<List<WeatherAlertResponse>>builder()
                .success(true)
                .message("Weather alerts fetched successfully")
                .data(weatherFacade.getDestinationAlerts(destinationUuid))
                .build();
    }

    @GetMapping("/crowd-predictions")
    public ApiResponse<List<CrowdPredictionResponse>> getDestinationCrowdPredictions(@PathVariable UUID destinationUuid) {
        return ApiResponse.<List<CrowdPredictionResponse>>builder()
                .success(true)
                .message("Crowd predictions fetched successfully")
                .data(weatherFacade.getDestinationCrowdPredictions(destinationUuid))
                .build();
    }
}
