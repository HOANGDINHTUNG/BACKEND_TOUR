package com.wedservice.backend.module.weather.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import com.wedservice.backend.module.weather.dto.request.AdminCreateRouteEstimateRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertCrowdPredictionRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertWeatherForecastRequest;
import com.wedservice.backend.module.weather.dto.request.AdminWeatherAlertRequest;
import com.wedservice.backend.module.weather.dto.request.UpdateWeatherAlertStatusRequest;
import com.wedservice.backend.module.weather.entity.CrowdPrediction;
import com.wedservice.backend.module.weather.entity.RouteEstimate;
import com.wedservice.backend.module.weather.entity.WeatherAlert;
import com.wedservice.backend.module.weather.entity.WeatherForecast;
import com.wedservice.backend.module.weather.entity.WeatherSeverity;
import com.wedservice.backend.module.weather.repository.CrowdPredictionRepository;
import com.wedservice.backend.module.weather.repository.RouteEstimateRepository;
import com.wedservice.backend.module.weather.repository.WeatherAlertRepository;
import com.wedservice.backend.module.weather.repository.WeatherForecastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWeatherServiceTest {

    @Mock
    private DestinationRepository destinationRepository;
    @Mock
    private WeatherForecastRepository weatherForecastRepository;
    @Mock
    private WeatherAlertRepository weatherAlertRepository;
    @Mock
    private CrowdPredictionRepository crowdPredictionRepository;
    @Mock
    private RouteEstimateRepository routeEstimateRepository;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    private AdminWeatherService adminWeatherService;

    @BeforeEach
    void setUp() {
        adminWeatherService = new AdminWeatherService(
                destinationRepository,
                weatherForecastRepository,
                weatherAlertRepository,
                crowdPredictionRepository,
                routeEstimateRepository,
                tourScheduleRepository,
                tourRepository,
                auditTrailRecorder
        );
    }

    @Test
    void upsertForecast_createsForecastAndRecordsAudit() {
        UUID destinationUuid = UUID.randomUUID();
        LocalDate forecastDate = LocalDate.of(2026, 4, 18);
        Destination destination = Destination.builder().id(9L).uuid(destinationUuid).build();

        when(destinationRepository.findByUuid(destinationUuid)).thenReturn(Optional.of(destination));
        when(weatherForecastRepository.findByDestinationIdAndForecastDate(destination.getId(), forecastDate))
                .thenReturn(Optional.empty());
        when(weatherForecastRepository.save(any(WeatherForecast.class))).thenAnswer(invocation -> {
            WeatherForecast forecast = invocation.getArgument(0);
            forecast.setId(33L);
            return forecast;
        });

        AdminUpsertWeatherForecastRequest request = AdminUpsertWeatherForecastRequest.builder()
                .summary("Co mua")
                .tempMin(BigDecimal.valueOf(24))
                .tempMax(BigDecimal.valueOf(31))
                .humidityPercent(BigDecimal.valueOf(80))
                .rawPayload("{\"source\":\"manual\"}")
                .build();

        assertEquals(33L, adminWeatherService.upsertForecast(destinationUuid, forecastDate, request).getId());
        verify(auditTrailRecorder).record(any(), eq(33L), any(), any());
    }

    @Test
    void upsertForecast_rejectsInvalidJsonPayload() {
        UUID destinationUuid = UUID.randomUUID();
        Destination destination = Destination.builder().id(9L).uuid(destinationUuid).build();

        when(destinationRepository.findByUuid(destinationUuid)).thenReturn(Optional.of(destination));

        AdminUpsertWeatherForecastRequest request = AdminUpsertWeatherForecastRequest.builder()
                .rawPayload("{invalid")
                .build();

        assertThrows(BadRequestException.class,
                () -> adminWeatherService.upsertForecast(destinationUuid, LocalDate.now(), request));
    }

    @Test
    void createAlert_rejectsScheduleFromAnotherDestination() {
        UUID destinationUuid = UUID.randomUUID();
        Destination destination = Destination.builder().id(9L).uuid(destinationUuid).build();
        TourSchedule schedule = TourSchedule.builder().id(20L).tourId(100L).build();
        Destination anotherDestination = Destination.builder().id(999L).build();
        Tour tour = Tour.builder().id(100L).destination(anotherDestination).build();

        when(destinationRepository.findByUuid(destinationUuid)).thenReturn(Optional.of(destination));
        when(tourScheduleRepository.findById(20L)).thenReturn(Optional.of(schedule));
        when(tourRepository.findById(100L)).thenReturn(Optional.of(tour));

        AdminWeatherAlertRequest request = AdminWeatherAlertRequest.builder()
                .scheduleId(20L)
                .severity(WeatherSeverity.WARNING)
                .alertType("rain")
                .title("Mua lon")
                .message("Han che di chuyen")
                .validFrom(LocalDateTime.now())
                .validTo(LocalDateTime.now().plusHours(3))
                .build();

        assertThrows(BadRequestException.class, () -> adminWeatherService.createAlert(destinationUuid, request));
    }

    @Test
    void updateAlertStatus_updatesActiveFlagAndRecordsAudit() {
        UUID destinationUuid = UUID.randomUUID();
        Destination destination = Destination.builder().id(9L).uuid(destinationUuid).build();
        WeatherAlert alert = WeatherAlert.builder()
                .id(50L)
                .destinationId(destination.getId())
                .severity(WeatherSeverity.INFO)
                .alertType("wind")
                .title("Gio lon")
                .message("Can than")
                .validFrom(LocalDateTime.now())
                .validTo(LocalDateTime.now().plusHours(2))
                .isActive(true)
                .build();

        when(destinationRepository.findByUuid(destinationUuid)).thenReturn(Optional.of(destination));
        when(weatherAlertRepository.findByIdAndDestinationId(50L, destination.getId())).thenReturn(Optional.of(alert));
        when(weatherAlertRepository.save(any(WeatherAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminWeatherService.updateAlertStatus(destinationUuid, 50L, UpdateWeatherAlertStatusRequest.builder()
                .active(false)
                .build());

        ArgumentCaptor<WeatherAlert> captor = ArgumentCaptor.forClass(WeatherAlert.class);
        verify(weatherAlertRepository).save(captor.capture());
        assertEquals(false, captor.getValue().getIsActive());
        verify(auditTrailRecorder).record(any(), eq(50L), any(), any());
    }

    @Test
    void upsertCrowdPrediction_createsPredictionAndRecordsAudit() {
        UUID destinationUuid = UUID.randomUUID();
        LocalDate predictionDate = LocalDate.of(2026, 4, 20);
        Destination destination = Destination.builder().id(9L).uuid(destinationUuid).build();

        when(destinationRepository.findByUuid(destinationUuid)).thenReturn(Optional.of(destination));
        when(crowdPredictionRepository.findByDestinationIdAndPredictionDate(destination.getId(), predictionDate))
                .thenReturn(Optional.empty());
        when(crowdPredictionRepository.save(any(CrowdPrediction.class))).thenAnswer(invocation -> {
            CrowdPrediction prediction = invocation.getArgument(0);
            prediction.setId(88L);
            return prediction;
        });

        assertEquals(88L, adminWeatherService.upsertCrowdPrediction(
                destinationUuid,
                predictionDate,
                AdminUpsertCrowdPredictionRequest.builder()
                        .crowdLevel(CrowdLevel.HIGH)
                        .predictedVisitors(12000)
                        .confidenceScore(BigDecimal.valueOf(84))
                        .reasonsJson("{\"holiday\":true}")
                        .build()
        ).getId());
        verify(auditTrailRecorder).record(any(), eq(88L), any(), any());
    }

    @Test
    void createRouteEstimate_rejectsHalfCoordinatePair() {
        assertThrows(BadRequestException.class, () -> adminWeatherService.createRouteEstimate(
                AdminCreateRouteEstimateRequest.builder()
                        .fromLabel("Ho Chi Minh City")
                        .toLabel("Da Lat")
                        .fromLatitude(BigDecimal.valueOf(10.77))
                        .distanceKm(BigDecimal.valueOf(300))
                        .durationMinutes(360)
                        .build()
        ));
    }
}
