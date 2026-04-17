package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.loyalty.dto.response.TravelPassportResponse;
import com.wedservice.backend.module.loyalty.facade.UserPassportFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserPassportControllerTest {

    @Mock
    private UserPassportFacade userPassportFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserPassportController(userPassportFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyPassport_returnsWrappedApiResponse() throws Exception {
        when(userPassportFacade.getMyPassport()).thenReturn(TravelPassportResponse.builder()
                .passportId(1L)
                .userId(UUID.randomUUID())
                .passportNo("TVP001")
                .badges(List.of())
                .visitedDestinations(List.of())
                .build());

        mockMvc.perform(get("/users/me/passport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Travel passport fetched successfully"))
                .andExpect(jsonPath("$.data.passportNo").value("TVP001"));
    }
}
