package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.loyalty.dto.response.UserCheckinResponse;
import com.wedservice.backend.module.loyalty.facade.UserCheckinFacade;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserCheckinControllerTest {

    @Mock
    private UserCheckinFacade userCheckinFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserCheckinController(userCheckinFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyCheckins_returnsWrappedApiResponse() throws Exception {
        when(userCheckinFacade.getMyCheckins()).thenReturn(List.of(
                UserCheckinResponse.builder()
                        .id(1L)
                        .bookingCode("BK100")
                        .destinationName("Da Nang")
                        .build()
        ));

        mockMvc.perform(get("/users/me/checkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User checkins fetched successfully"))
                .andExpect(jsonPath("$.data[0].bookingCode").value("BK100"));
    }
}
