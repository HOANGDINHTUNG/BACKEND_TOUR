package com.wedservice.backend.module.weather.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.weather.dto.request.AdminCreateRouteEstimateRequest;
import com.wedservice.backend.module.weather.dto.response.RouteEstimateResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminRouteEstimateControllerTest {

    @Mock
    private AdminWeatherFacade adminWeatherFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminRouteEstimateController(adminWeatherFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createRouteEstimate_returnsWrappedApiResponse() throws Exception {
        when(adminWeatherFacade.createRouteEstimate(any(AdminCreateRouteEstimateRequest.class)))
                .thenReturn(RouteEstimateResponse.builder()
                        .id(9L)
                        .fromLabel("Ho Chi Minh City")
                        .toLabel("Da Lat")
                        .build());

        mockMvc.perform(post("/admin/route-estimates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AdminCreateRouteEstimateRequest.builder()
                                .fromLabel("Ho Chi Minh City")
                                .toLabel("Da Lat")
                                .durationMinutes(360)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Route estimate created successfully"))
                .andExpect(jsonPath("$.data.id").value(9L));
    }
}
