package com.wedservice.backend.module.weather.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertCrowdPredictionRequest;
import com.wedservice.backend.module.weather.dto.request.AdminUpsertWeatherForecastRequest;
import com.wedservice.backend.module.weather.dto.request.UpdateWeatherAlertStatusRequest;
import com.wedservice.backend.module.weather.dto.response.CrowdPredictionResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherAlertResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherForecastResponse;
import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.weather.entity.WeatherSeverity;
import com.wedservice.backend.module.weather.facade.AdminWeatherFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminWeatherControllerTest {

    @Mock
    private AdminWeatherFacade adminWeatherFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminWeatherController(adminWeatherFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void upsertForecast_returnsWrappedApiResponse() throws Exception {
        UUID destinationUuid = UUID.randomUUID();
        LocalDate forecastDate = LocalDate.of(2026, 4, 18);

        when(adminWeatherFacade.upsertForecast(eq(destinationUuid), eq(forecastDate), any(AdminUpsertWeatherForecastRequest.class)))
                .thenReturn(WeatherForecastResponse.builder()
                        .id(1L)
                        .forecastDate(forecastDate)
                        .summary("Mua rai rac")
                        .build());

        mockMvc.perform(put("/admin/destinations/{destinationUuid}/weather/forecasts/{forecastDate}", destinationUuid, forecastDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AdminUpsertWeatherForecastRequest.builder()
                                .summary("Mua rai rac")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Weather forecast saved successfully"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void updateAlertStatus_returnsWrappedApiResponse() throws Exception {
        UUID destinationUuid = UUID.randomUUID();

        when(adminWeatherFacade.updateAlertStatus(eq(destinationUuid), eq(9L), any(UpdateWeatherAlertStatusRequest.class)))
                .thenReturn(WeatherAlertResponse.builder()
                        .id(9L)
                        .severity(WeatherSeverity.WARNING)
                        .title("Mua to")
                        .validFrom(LocalDateTime.of(2026, 4, 18, 9, 0))
                        .validTo(LocalDateTime.of(2026, 4, 18, 12, 0))
                        .isActive(false)
                        .build());

        mockMvc.perform(patch("/admin/destinations/{destinationUuid}/weather/alerts/{alertId}/status", destinationUuid, 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateWeatherAlertStatusRequest.builder()
                                .active(false)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Weather alert status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    void upsertCrowdPrediction_returnsWrappedApiResponse() throws Exception {
        UUID destinationUuid = UUID.randomUUID();
        LocalDate predictionDate = LocalDate.of(2026, 4, 20);

        when(adminWeatherFacade.upsertCrowdPrediction(eq(destinationUuid), eq(predictionDate), any(AdminUpsertCrowdPredictionRequest.class)))
                .thenReturn(CrowdPredictionResponse.builder()
                        .id(11L)
                        .predictionDate(predictionDate)
                        .crowdLevel("high")
                        .build());

        mockMvc.perform(put("/admin/destinations/{destinationUuid}/weather/crowd-predictions/{predictionDate}", destinationUuid, predictionDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AdminUpsertCrowdPredictionRequest.builder()
                                .crowdLevel(CrowdLevel.HIGH)
                                .predictedVisitors(10000)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Crowd prediction saved successfully"))
                .andExpect(jsonPath("$.data.id").value(11L));
    }
}
