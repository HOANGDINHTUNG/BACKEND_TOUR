package com.wedservice.backend.module.weather.facade;

import com.wedservice.backend.module.weather.dto.response.WeatherAlertResponse;
import com.wedservice.backend.module.weather.dto.response.CrowdPredictionResponse;
import com.wedservice.backend.module.weather.dto.response.RouteEstimateResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherForecastResponse;
import com.wedservice.backend.module.weather.service.PublicWeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WeatherFacade {

    private final PublicWeatherService publicWeatherService;

    public List<WeatherForecastResponse> getDestinationForecasts(UUID destinationUuid) {
        return publicWeatherService.getDestinationForecasts(destinationUuid);
    }

    public List<WeatherAlertResponse> getDestinationAlerts(UUID destinationUuid) {
        return publicWeatherService.getDestinationAlerts(destinationUuid);
    }

    public List<CrowdPredictionResponse> getDestinationCrowdPredictions(UUID destinationUuid) {
        return publicWeatherService.getDestinationCrowdPredictions(destinationUuid);
    }

    public List<RouteEstimateResponse> getRouteEstimates(String fromLabel, String toLabel) {
        return publicWeatherService.getRouteEstimates(fromLabel, toLabel);
    }
}
