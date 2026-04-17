package com.wedservice.backend.module.support.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.support.dto.request.CreateSupportSessionRequest;
import com.wedservice.backend.module.support.dto.response.SupportMessageResponse;
import com.wedservice.backend.module.support.dto.response.SupportSessionResponse;
import com.wedservice.backend.module.support.entity.SupportSenderType;
import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import com.wedservice.backend.module.support.facade.UserSupportFacade;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserSupportControllerTest {

    @Mock
    private UserSupportFacade userSupportFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserSupportController(userSupportFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createMySupportSession_returnsWrappedApiResponse() throws Exception {
        when(userSupportFacade.createMySupportSession(any(CreateSupportSessionRequest.class))).thenReturn(
                SupportSessionResponse.builder()
                        .id(1L)
                        .sessionCode("CS123")
                        .status(SupportSessionStatus.OPEN)
                        .messages(List.of(SupportMessageResponse.builder()
                                .id(2L)
                                .senderType(SupportSenderType.CUSTOMER)
                                .messageText("Xin ho tro")
                                .build()))
                        .build()
        );

        mockMvc.perform(post("/users/me/support/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateSupportSessionRequest.builder()
                                .initialMessage("Xin ho tro")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Support session created successfully"))
                .andExpect(jsonPath("$.data.sessionCode").value("CS123"));
    }

    @Test
    void getMySupportSessions_returnsWrappedApiResponse() throws Exception {
        when(userSupportFacade.getMySupportSessions()).thenReturn(List.of(
                SupportSessionResponse.builder()
                        .id(1L)
                        .sessionCode("CS123")
                        .userId(UUID.randomUUID())
                        .status(SupportSessionStatus.WAITING_STAFF)
                        .createdAt(LocalDateTime.of(2026, 4, 17, 10, 0))
                        .build()
        ));

        mockMvc.perform(get("/users/me/support/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Support sessions fetched successfully"))
                .andExpect(jsonPath("$.data[0].sessionCode").value("CS123"));
    }
}
