package com.wedservice.backend.module.weather.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.weather.dto.response.CrowdPredictionResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherAlertResponse;
import com.wedservice.backend.module.weather.dto.response.WeatherForecastResponse;
import com.wedservice.backend.module.weather.entity.WeatherSeverity;
import com.wedservice.backend.module.weather.facade.WeatherFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @Mock
    private WeatherFacade weatherFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new WeatherController(weatherFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getDestinationForecasts_returnsWrappedApiResponse() throws Exception {
        UUID destinationUuid = UUID.randomUUID();
        when(weatherFacade.getDestinationForecasts(destinationUuid)).thenReturn(List.of(
                WeatherForecastResponse.builder()
                        .id(1L)
                        .forecastDate(LocalDate.of(2026, 4, 18))
                        .summary("Nang nhe")
                        .build()
        ));

        mockMvc.perform(get("/destinations/{destinationUuid}/weather/forecasts", destinationUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Weather forecasts fetched successfully"))
                .andExpect(jsonPath("$.data[0].summary").value("Nang nhe"));
    }

    @Test
    void getDestinationAlerts_returnsWrappedApiResponse() throws Exception {
        UUID destinationUuid = UUID.randomUUID();
        when(weatherFacade.getDestinationAlerts(destinationUuid)).thenReturn(List.of(
                WeatherAlertResponse.builder()
                        .id(2L)
                        .severity(WeatherSeverity.WARNING)
                        .title("Mua lon")
                        .validFrom(LocalDateTime.of(2026, 4, 18, 9, 0))
                        .validTo(LocalDateTime.of(2026, 4, 18, 12, 0))
                        .build()
        ));

        mockMvc.perform(get("/destinations/{destinationUuid}/weather/alerts", destinationUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Weather alerts fetched successfully"))
                .andExpect(jsonPath("$.data[0].severity").value("warning"));
    }

    @Test
    void getDestinationCrowdPredictions_returnsWrappedApiResponse() throws Exception {
        UUID destinationUuid = UUID.randomUUID();
        when(weatherFacade.getDestinationCrowdPredictions(destinationUuid)).thenReturn(List.of(
                CrowdPredictionResponse.builder()
                        .id(5L)
                        .crowdLevel("high")
                        .build()
        ));

        mockMvc.perform(get("/destinations/{destinationUuid}/weather/crowd-predictions", destinationUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Crowd predictions fetched successfully"))
                .andExpect(jsonPath("$.data[0].crowdLevel").value("high"));
    }
}
