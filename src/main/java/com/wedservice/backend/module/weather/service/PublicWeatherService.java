package com.wedservice.backend.module.weather.service;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.weather.dto.response.CrowdPredictionResponse;
import com.wedservice.backend.module.weather.dto.response.RouteEstimateResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherAlertResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherForecastResponse;
import com.wedservice.backend.module.weather.entity.CrowdPrediction;
import com.wedservice.backend.module.weather.entity.RouteEstimate;
import com.wedservice.backend.module.weather.entity.WeatherAlert;
import com.wedservice.backend.module.weather.entity.WeatherForecast;
import com.wedservice.backend.module.weather.repository.CrowdPredictionRepository;
import com.wedservice.backend.module.weather.repository.RouteEstimateRepository;
import com.wedservice.backend.module.weather.repository.WeatherAlertRepository;
import com.wedservice.backend.module.weather.repository.WeatherForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicWeatherService {

    private final DestinationRepository destinationRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final WeatherAlertRepository weatherAlertRepository;
    private final CrowdPredictionRepository crowdPredictionRepository;
    private final RouteEstimateRepository routeEstimateRepository;

    @Transactional(readOnly = true)
    public List<WeatherForecastResponse> getDestinationForecasts(UUID destinationUuid) {
        Destination destination = findPublicDestination(destinationUuid);
        return weatherForecastRepository.findByDestinationIdAndForecastDateGreaterThanEqualOrderByForecastDateAsc(
                        destination.getId(),
                        LocalDate.now()
                ).stream()
                .map(this::toForecastResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WeatherAlertResponse> getDestinationAlerts(UUID destinationUuid) {
        Destination destination = findPublicDestination(destinationUuid);
        LocalDateTime now = LocalDateTime.now();
        return weatherAlertRepository
                .findByDestinationIdAndIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByValidFromDesc(
                        destination.getId(),
                        now,
                        now
                ).stream()
                .map(this::toAlertResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CrowdPredictionResponse> getDestinationCrowdPredictions(UUID destinationUuid) {
        Destination destination = findPublicDestination(destinationUuid);
        return crowdPredictionRepository.findByDestinationIdAndPredictionDateGreaterThanEqualOrderByPredictionDateAsc(
                        destination.getId(),
                        LocalDate.now()
                ).stream()
                .map(this::toCrowdPredictionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RouteEstimateResponse> getRouteEstimates(String fromLabel, String toLabel) {
        String normalizedFromLabel = normalizeLabel(fromLabel);
        String normalizedToLabel = normalizeLabel(toLabel);
        return routeEstimateRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .filter(item -> matchesLabel(item.getFromLabel(), normalizedFromLabel))
                .filter(item -> matchesLabel(item.getToLabel(), normalizedToLabel))
                .limit(20)
                .map(this::toRouteEstimateResponse)
                .toList();
    }

    private Destination findPublicDestination(UUID destinationUuid) {
        Destination destination = destinationRepository.findByUuid(destinationUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + destinationUuid));

        if (destination.getDeletedAt() != null
                || destination.getStatus() != DestinationStatus.APPROVED
                || !Boolean.TRUE.equals(destination.getIsActive())) {
            throw new ResourceNotFoundException("Destination not found with uuid: " + destinationUuid);
        }
        return destination;
    }

    private WeatherForecastResponse toForecastResponse(WeatherForecast forecast) {
        return WeatherForecastResponse.builder()
                .id(forecast.getId())
                .destinationId(forecast.getDestinationId())
                .forecastDate(forecast.getForecastDate())
                .weatherCode(forecast.getWeatherCode())
                .summary(forecast.getSummary())
                .tempMin(forecast.getTempMin())
                .tempMax(forecast.getTempMax())
                .humidityPercent(forecast.getHumidityPercent())
                .windSpeed(forecast.getWindSpeed())
                .rainProbability(forecast.getRainProbability())
                .sourceName(forecast.getSourceName())
                .rawPayload(forecast.getRawPayload())
                .createdAt(forecast.getCreatedAt())
                .build();
    }

    private WeatherAlertResponse toAlertResponse(WeatherAlert alert) {
        return WeatherAlertResponse.builder()
                .id(alert.getId())
                .destinationId(alert.getDestinationId())
                .scheduleId(alert.getScheduleId())
                .severity(alert.getSeverity())
                .alertType(alert.getAlertType())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .actionAdvice(alert.getActionAdvice())
                .validFrom(alert.getValidFrom())
                .validTo(alert.getValidTo())
                .isActive(alert.getIsActive())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private CrowdPredictionResponse toCrowdPredictionResponse(CrowdPrediction prediction) {
        return CrowdPredictionResponse.builder()
                .id(prediction.getId())
                .destinationId(prediction.getDestinationId())
                .predictionDate(prediction.getPredictionDate())
                .crowdLevel(prediction.getCrowdLevel() == null ? null : prediction.getCrowdLevel().getValue())
                .predictedVisitors(prediction.getPredictedVisitors())
                .confidenceScore(prediction.getConfidenceScore())
                .reasonsJson(prediction.getReasonsJson())
                .createdAt(prediction.getCreatedAt())
                .build();
    }

    private RouteEstimateResponse toRouteEstimateResponse(RouteEstimate routeEstimate) {
        return RouteEstimateResponse.builder()
                .id(routeEstimate.getId())
                .fromLabel(routeEstimate.getFromLabel())
                .toLabel(routeEstimate.getToLabel())
                .fromLatitude(routeEstimate.getFromLatitude())
                .fromLongitude(routeEstimate.getFromLongitude())
                .toLatitude(routeEstimate.getToLatitude())
                .toLongitude(routeEstimate.getToLongitude())
                .distanceKm(routeEstimate.getDistanceKm())
                .durationMinutes(routeEstimate.getDurationMinutes())
                .googleMapUrl(routeEstimate.getGoogleMapUrl())
                .sourceName(routeEstimate.getSourceName())
                .createdAt(routeEstimate.getCreatedAt())
                .build();
    }

    private String normalizeLabel(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : null;
    }

    private boolean matchesLabel(String sourceValue, String normalizedFilter) {
        if (!StringUtils.hasText(normalizedFilter)) {
            return true;
        }
        return StringUtils.hasText(sourceValue) && sourceValue.toLowerCase().contains(normalizedFilter);
    }
}
