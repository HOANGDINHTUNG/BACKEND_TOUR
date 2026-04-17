package com.wedservice.backend.module.weather.facade;

import com.wedservice.backend.module.weather.dto.request.AdminCreateRouteEstimateRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertCrowdPredictionRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertWeatherForecastRequest;
import com.wedservice.backend.module.weather.dto.request.AdminWeatherAlertRequest;
import com.wedservice.backend.module.weather.dto.request.UpdateWeatherAlertStatusRequest;
import com.wedservice.backend.module.weather.dto.response.CrowdPredictionResponse;
import com.wedservice.backend.module.weather.dto.response.RouteEstimateResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherAlertResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherForecastResponse;
import com.wedservice.backend.module.weather.service.AdminWeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminWeatherFacade {

    private final AdminWeatherService adminWeatherService;

    public List<WeatherForecastResponse> getForecasts(UUID destinationUuid) {
        return adminWeatherService.getForecasts(destinationUuid);
    }

    public WeatherForecastResponse upsertForecast(
            UUID destinationUuid,
            LocalDate forecastDate,
            AdminUpsertWeatherForecastRequest request
    ) {
        return adminWeatherService.upsertForecast(destinationUuid, forecastDate, request);
    }

    public List<WeatherAlertResponse> getAlerts(UUID destinationUuid) {
        return adminWeatherService.getAlerts(destinationUuid);
    }

    public WeatherAlertResponse createAlert(UUID destinationUuid, AdminWeatherAlertRequest request) {
        return adminWeatherService.createAlert(destinationUuid, request);
    }

    public WeatherAlertResponse updateAlert(UUID destinationUuid, Long alertId, AdminWeatherAlertRequest request) {
        return adminWeatherService.updateAlert(destinationUuid, alertId, request);
    }

    public WeatherAlertResponse updateAlertStatus(
            UUID destinationUuid,
            Long alertId,
            UpdateWeatherAlertStatusRequest request
    ) {
        return adminWeatherService.updateAlertStatus(destinationUuid, alertId, request);
    }

    public List<CrowdPredictionResponse> getCrowdPredictions(UUID destinationUuid) {
        return adminWeatherService.getCrowdPredictions(destinationUuid);
    }

    public CrowdPredictionResponse upsertCrowdPrediction(
            UUID destinationUuid,
            LocalDate predictionDate,
            AdminUpsertCrowdPredictionRequest request
    ) {
        return adminWeatherService.upsertCrowdPrediction(destinationUuid, predictionDate, request);
    }

    public List<RouteEstimateResponse> getRouteEstimates(String fromLabel, String toLabel) {
        return adminWeatherService.getRouteEstimates(fromLabel, toLabel);
    }

    public RouteEstimateResponse createRouteEstimate(AdminCreateRouteEstimateRequest request) {
        return adminWeatherService.createRouteEstimate(request);
    }
}
