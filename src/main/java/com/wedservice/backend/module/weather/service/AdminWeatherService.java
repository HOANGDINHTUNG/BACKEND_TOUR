package com.wedservice.backend.module.weather.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import com.wedservice.backend.module.weather.dto.request.AdminCreateRouteEstimateRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertCrowdPredictionRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertWeatherForecastRequest;
import com.wedservice.backend.module.weather.dto.request.AdminWeatherAlertRequest;
import com.wedservice.backend.module.weather.dto.request.UpdateWeatherAlertStatusRequest;
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
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminWeatherService {

    private final DestinationRepository destinationRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final WeatherAlertRepository weatherAlertRepository;
    private final CrowdPredictionRepository crowdPredictionRepository;
    private final RouteEstimateRepository routeEstimateRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourRepository tourRepository;
    private final AuditTrailRecorder auditTrailRecorder;
    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Transactional(readOnly = true)
    public List<WeatherForecastResponse> getForecasts(UUID destinationUuid) {
        Destination destination = findAdminDestination(destinationUuid);
        return weatherForecastRepository.findByDestinationIdOrderByForecastDateAsc(destination.getId()).stream()
                .map(this::toForecastResponse)
                .toList();
    }

    @Transactional
    public WeatherForecastResponse upsertForecast(
            UUID destinationUuid,
            LocalDate forecastDate,
            AdminUpsertWeatherForecastRequest request
    ) {
        Destination destination = findAdminDestination(destinationUuid);
        validateForecastRequest(forecastDate, request);

        WeatherForecast forecast = weatherForecastRepository
                .findByDestinationIdAndForecastDate(destination.getId(), forecastDate)
                .orElseGet(() -> WeatherForecast.builder()
                        .destinationId(destination.getId())
                        .forecastDate(forecastDate)
                        .build());

        WeatherForecastResponse oldSnapshot = forecast.getId() == null ? null : toForecastResponse(forecast);
        applyForecast(forecast, request);

        WeatherForecast saved = weatherForecastRepository.save(forecast);
        WeatherForecastResponse response = toForecastResponse(saved);
        auditTrailRecorder.record(AuditActionType.WEATHER_FORECAST_UPSERT, saved.getId(), oldSnapshot, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<WeatherAlertResponse> getAlerts(UUID destinationUuid) {
        Destination destination = findAdminDestination(destinationUuid);
        return weatherAlertRepository.findByDestinationIdOrderByValidFromDesc(destination.getId()).stream()
                .map(this::toAlertResponse)
                .toList();
    }

    @Transactional
    public WeatherAlertResponse createAlert(UUID destinationUuid, AdminWeatherAlertRequest request) {
        Destination destination = findAdminDestination(destinationUuid);
        validateAlertRequest(destination, request);

        WeatherAlert alert = WeatherAlert.builder()
                .destinationId(destination.getId())
                .build();
        applyAlert(alert, request);

        WeatherAlert saved = weatherAlertRepository.save(alert);
        WeatherAlertResponse response = toAlertResponse(saved);
        auditTrailRecorder.record(AuditActionType.WEATHER_ALERT_CREATE, saved.getId(), null, response);
        return response;
    }

    @Transactional
    public WeatherAlertResponse updateAlert(UUID destinationUuid, Long alertId, AdminWeatherAlertRequest request) {
        Destination destination = findAdminDestination(destinationUuid);
        WeatherAlert alert = findAlert(destination.getId(), alertId);
        validateAlertRequest(destination, request);

        WeatherAlertResponse oldSnapshot = toAlertResponse(alert);
        applyAlert(alert, request);

        WeatherAlert saved = weatherAlertRepository.save(alert);
        WeatherAlertResponse response = toAlertResponse(saved);
        auditTrailRecorder.record(AuditActionType.WEATHER_ALERT_UPDATE, saved.getId(), oldSnapshot, response);
        return response;
    }

    @Transactional
    public WeatherAlertResponse updateAlertStatus(
            UUID destinationUuid,
            Long alertId,
            UpdateWeatherAlertStatusRequest request
    ) {
        Destination destination = findAdminDestination(destinationUuid);
        WeatherAlert alert = findAlert(destination.getId(), alertId);

        WeatherAlertResponse oldSnapshot = toAlertResponse(alert);
        alert.setIsActive(Boolean.TRUE.equals(request.getActive()));

        WeatherAlert saved = weatherAlertRepository.save(alert);
        WeatherAlertResponse response = toAlertResponse(saved);
        auditTrailRecorder.record(AuditActionType.WEATHER_ALERT_STATUS_UPDATE, saved.getId(), oldSnapshot, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<CrowdPredictionResponse> getCrowdPredictions(UUID destinationUuid) {
        Destination destination = findAdminDestination(destinationUuid);
        return crowdPredictionRepository.findByDestinationIdOrderByPredictionDateAsc(destination.getId()).stream()
                .map(this::toCrowdPredictionResponse)
                .toList();
    }

    @Transactional
    public CrowdPredictionResponse upsertCrowdPrediction(
            UUID destinationUuid,
            LocalDate predictionDate,
            AdminUpsertCrowdPredictionRequest request
    ) {
        Destination destination = findAdminDestination(destinationUuid);
        validateCrowdPredictionRequest(predictionDate, request);

        CrowdPrediction prediction = crowdPredictionRepository
                .findByDestinationIdAndPredictionDate(destination.getId(), predictionDate)
                .orElseGet(() -> CrowdPrediction.builder()
                        .destinationId(destination.getId())
                        .predictionDate(predictionDate)
                        .build());

        CrowdPredictionResponse oldSnapshot = prediction.getId() == null ? null : toCrowdPredictionResponse(prediction);
        prediction.setCrowdLevel(request.getCrowdLevel());
        prediction.setPredictedVisitors(request.getPredictedVisitors());
        prediction.setConfidenceScore(request.getConfidenceScore());
        prediction.setReasonsJson(trimToNull(request.getReasonsJson()));

        CrowdPrediction saved = crowdPredictionRepository.save(prediction);
        CrowdPredictionResponse response = toCrowdPredictionResponse(saved);
        auditTrailRecorder.record(AuditActionType.CROWD_PREDICTION_UPSERT, saved.getId(), oldSnapshot, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<RouteEstimateResponse> getRouteEstimates(String fromLabel, String toLabel) {
        String normalizedFromLabel = normalizeRouteLabel(fromLabel);
        String normalizedToLabel = normalizeRouteLabel(toLabel);
        return routeEstimateRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .filter(item -> matchesRouteLabel(item.getFromLabel(), normalizedFromLabel))
                .filter(item -> matchesRouteLabel(item.getToLabel(), normalizedToLabel))
                .limit(50)
                .map(this::toRouteEstimateResponse)
                .toList();
    }

    @Transactional
    public RouteEstimateResponse createRouteEstimate(AdminCreateRouteEstimateRequest request) {
        validateRouteEstimateRequest(request);

        RouteEstimate routeEstimate = RouteEstimate.builder()
                .fromLabel(normalizeRequiredRouteLabel(request.getFromLabel(), "fromLabel"))
                .toLabel(normalizeRequiredRouteLabel(request.getToLabel(), "toLabel"))
                .fromLatitude(request.getFromLatitude())
                .fromLongitude(request.getFromLongitude())
                .toLatitude(request.getToLatitude())
                .toLongitude(request.getToLongitude())
                .distanceKm(request.getDistanceKm())
                .durationMinutes(request.getDurationMinutes())
                .googleMapUrl(trimToNull(request.getGoogleMapUrl()))
                .sourceName(trimToNull(request.getSourceName()))
                .build();

        RouteEstimate saved = routeEstimateRepository.save(routeEstimate);
        RouteEstimateResponse response = toRouteEstimateResponse(saved);
        auditTrailRecorder.record(AuditActionType.ROUTE_ESTIMATE_CREATE, saved.getId(), null, response);
        return response;
    }

    private void applyForecast(WeatherForecast forecast, AdminUpsertWeatherForecastRequest request) {
        forecast.setWeatherCode(trimToNull(request.getWeatherCode()));
        forecast.setSummary(trimToNull(request.getSummary()));
        forecast.setTempMin(request.getTempMin());
        forecast.setTempMax(request.getTempMax());
        forecast.setHumidityPercent(request.getHumidityPercent());
        forecast.setWindSpeed(request.getWindSpeed());
        forecast.setRainProbability(request.getRainProbability());
        forecast.setSourceName(trimToNull(request.getSourceName()));
        forecast.setRawPayload(trimToNull(request.getRawPayload()));
    }

    private void applyAlert(WeatherAlert alert, AdminWeatherAlertRequest request) {
        alert.setScheduleId(request.getScheduleId());
        alert.setSeverity(request.getSeverity());
        alert.setAlertType(request.getAlertType().trim());
        alert.setTitle(request.getTitle().trim());
        alert.setMessage(request.getMessage().trim());
        alert.setActionAdvice(trimToNull(request.getActionAdvice()));
        alert.setValidFrom(request.getValidFrom());
        alert.setValidTo(request.getValidTo());
        alert.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());
    }

    private void validateForecastRequest(LocalDate forecastDate, AdminUpsertWeatherForecastRequest request) {
        if (forecastDate == null) {
            throw new BadRequestException("forecastDate is required");
        }
        validateNonNegative(request.getHumidityPercent(), "humidityPercent");
        validateNonNegative(request.getWindSpeed(), "windSpeed");
        validateNonNegative(request.getRainProbability(), "rainProbability");
        validatePercent(request.getHumidityPercent(), "humidityPercent");
        validatePercent(request.getRainProbability(), "rainProbability");
        if (request.getTempMin() != null && request.getTempMax() != null
                && request.getTempMin().compareTo(request.getTempMax()) > 0) {
            throw new BadRequestException("tempMin cannot be greater than tempMax");
        }
        validateJson(request.getRawPayload(), "rawPayload");
    }

    private void validateAlertRequest(Destination destination, AdminWeatherAlertRequest request) {
        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BadRequestException("validTo must be greater than or equal to validFrom");
        }
        if (request.getScheduleId() != null) {
            validateScheduleDestination(destination, request.getScheduleId());
        }
    }

    private void validateCrowdPredictionRequest(LocalDate predictionDate, AdminUpsertCrowdPredictionRequest request) {
        if (predictionDate == null) {
            throw new BadRequestException("predictionDate is required");
        }
        if (request.getCrowdLevel() == null) {
            throw new BadRequestException("crowdLevel is required");
        }
        if (request.getPredictedVisitors() != null && request.getPredictedVisitors() < 0) {
            throw new BadRequestException("predictedVisitors must be greater than or equal to 0");
        }
        validateNonNegative(request.getConfidenceScore(), "confidenceScore");
        validatePercent(request.getConfidenceScore(), "confidenceScore");
        validateJson(request.getReasonsJson(), "reasonsJson");
    }

    private void validateRouteEstimateRequest(AdminCreateRouteEstimateRequest request) {
        validateCoordinatePair(request.getFromLatitude(), request.getFromLongitude(), "from");
        validateCoordinatePair(request.getToLatitude(), request.getToLongitude(), "to");
        validateLatitude(request.getFromLatitude(), "fromLatitude");
        validateLongitude(request.getFromLongitude(), "fromLongitude");
        validateLatitude(request.getToLatitude(), "toLatitude");
        validateLongitude(request.getToLongitude(), "toLongitude");
        validateNonNegative(request.getDistanceKm(), "distanceKm");
        if (request.getDurationMinutes() != null && request.getDurationMinutes() < 0) {
            throw new BadRequestException("durationMinutes must be greater than or equal to 0");
        }
    }

    private void validateScheduleDestination(Destination destination, Long scheduleId) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour schedule not found with id: " + scheduleId));
        Tour tour = tourRepository.findById(schedule.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + schedule.getTourId()));

        Long tourDestinationId = tour.getDestination() == null ? null : tour.getDestination().getId();
        if (!destination.getId().equals(tourDestinationId)) {
            throw new BadRequestException("scheduleId does not belong to the destination");
        }
    }

    private void validateNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(fieldName + " must be greater than or equal to 0");
        }
    }

    private void validatePercent(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException(fieldName + " must be less than or equal to 100");
        }
    }

    private void validateLatitude(BigDecimal value, String fieldName) {
        if (value == null) {
            return;
        }
        if (value.compareTo(BigDecimal.valueOf(-90)) < 0 || value.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BadRequestException(fieldName + " must be between -90 and 90");
        }
    }

    private void validateLongitude(BigDecimal value, String fieldName) {
        if (value == null) {
            return;
        }
        if (value.compareTo(BigDecimal.valueOf(-180)) < 0 || value.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BadRequestException(fieldName + " must be between -180 and 180");
        }
    }

    private void validateCoordinatePair(BigDecimal latitude, BigDecimal longitude, String prefix) {
        if ((latitude == null) != (longitude == null)) {
            throw new BadRequestException(prefix + " latitude/longitude must be provided together");
        }
    }

    private void validateJson(String rawJson, String fieldName) {
        if (!StringUtils.hasText(rawJson)) {
            return;
        }
        try {
            jsonMapper.readTree(rawJson);
        } catch (Exception ex) {
            throw new BadRequestException(fieldName + " must be valid JSON");
        }
    }

    private Destination findAdminDestination(UUID destinationUuid) {
        Destination destination = destinationRepository.findByUuid(destinationUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + destinationUuid));

        if (destination.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Destination not found with uuid: " + destinationUuid);
        }
        return destination;
    }

    private WeatherAlert findAlert(Long destinationId, Long alertId) {
        return weatherAlertRepository.findByIdAndDestinationId(alertId, destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Weather alert not found with id: " + alertId));
    }

    private String trimToNull(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private String normalizeRequiredRouteLabel(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeRouteLabel(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : null;
    }

    private boolean matchesRouteLabel(String sourceValue, String normalizedFilter) {
        if (!StringUtils.hasText(normalizedFilter)) {
            return true;
        }
        return StringUtils.hasText(sourceValue) && sourceValue.toLowerCase().contains(normalizedFilter);
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
}
