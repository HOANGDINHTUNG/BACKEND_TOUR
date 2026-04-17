package com.wedservice.backend.module.support.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.support.dto.request.AssignSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.UpdateSupportSessionStatusRequest;
import com.wedservice.backend.module.support.dto.response.SupportSessionResponse;
import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import com.wedservice.backend.module.support.facade.AdminSupportFacade;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSupportControllerTest {

    @Mock
    private AdminSupportFacade adminSupportFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminSupportController(adminSupportFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getSupportSessions_returnsWrappedApiResponse() throws Exception {
        when(adminSupportFacade.getSupportSessions(any(), any(), any())).thenReturn(List.of(
                SupportSessionResponse.builder()
                        .id(1L)
                        .sessionCode("CS123")
                        .status(SupportSessionStatus.OPEN)
                        .build()
        ));

        mockMvc.perform(get("/support/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Support sessions fetched successfully"))
                .andExpect(jsonPath("$.data[0].sessionCode").value("CS123"));
    }

    @Test
    void assignSupportSession_returnsWrappedApiResponse() throws Exception {
        UUID staffId = UUID.randomUUID();
        when(adminSupportFacade.assignSupportSession(any(Long.class), any(AssignSupportSessionRequest.class))).thenReturn(
                SupportSessionResponse.builder()
                        .id(1L)
                        .sessionCode("CS123")
                        .assignedStaffId(staffId)
                        .status(SupportSessionStatus.OPEN)
                        .build()
        );

        mockMvc.perform(patch("/support/sessions/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AssignSupportSessionRequest.builder()
                                .assignedStaffId(staffId.toString())
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Support session assigned successfully"))
                .andExpect(jsonPath("$.data.assignedStaffId").value(staffId.toString()));
    }

    @Test
    void updateSupportSessionStatus_returnsWrappedApiResponse() throws Exception {
        when(adminSupportFacade.updateSupportSessionStatus(any(Long.class), any(UpdateSupportSessionStatusRequest.class))).thenReturn(
                SupportSessionResponse.builder()
                        .id(1L)
                        .sessionCode("CS123")
                        .status(SupportSessionStatus.RESOLVED)
                        .build()
        );

        mockMvc.perform(patch("/support/sessions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateSupportSessionStatusRequest.builder()
                                .status(SupportSessionStatus.RESOLVED)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Support session status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("resolved"));
    }
}
