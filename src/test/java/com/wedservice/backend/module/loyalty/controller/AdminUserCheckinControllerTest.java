package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.loyalty.dto.request.CreateUserCheckinRequest;
import com.wedservice.backend.module.loyalty.dto.response.UserCheckinResponse;
import com.wedservice.backend.module.loyalty.facade.AdminUserCheckinFacade;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserCheckinControllerTest {

    @Mock
    private AdminUserCheckinFacade adminUserCheckinFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserCheckinController(adminUserCheckinFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createCheckin_returnsWrappedApiResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(adminUserCheckinFacade.createCheckin(eq(userId), any(CreateUserCheckinRequest.class))).thenReturn(
                UserCheckinResponse.builder()
                        .id(5L)
                        .bookingId(12L)
                        .destinationName("Hoi An")
                        .build()
        );

        mockMvc.perform(post("/users/{userId}/checkins", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateUserCheckinRequest.builder()
                                .bookingId(12L)
                                .note("Arrived")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User checkin created successfully"))
                .andExpect(jsonPath("$.data.id").value(5L));
    }
}
