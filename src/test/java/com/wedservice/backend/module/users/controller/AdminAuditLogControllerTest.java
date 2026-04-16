package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.response.AuditLogResponse;
import com.wedservice.backend.module.users.facade.AdminAuditLogFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminAuditLogControllerTest {

    @Mock
    private AdminAuditLogFacade adminAuditLogFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminAuditLogController(adminAuditLogFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getAuditLogs_returnsWrappedApiResponse() throws Exception {
        AuditLogResponse auditLog = AuditLogResponse.builder()
                .id(1L)
                .actorUserId(UUID.randomUUID())
                .actionName("role.update")
                .entityName("roles")
                .entityId("5")
                .oldData(JsonNodeFactory.instance.objectNode().put("code", "EDITOR"))
                .newData(JsonNodeFactory.instance.objectNode().put("code", "EDITOR_PLUS"))
                .ipAddress("127.0.0.1")
                .userAgent("JUnit")
                .createdAt(LocalDateTime.of(2026, 4, 16, 13, 0))
                .build();

        when(adminAuditLogFacade.getAuditLogs(any())).thenReturn(
                PageResponse.<AuditLogResponse>builder()
                        .content(List.of(auditLog))
                        .page(0)
                        .size(20)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build()
        );

        mockMvc.perform(get("/audit-logs")
                        .queryParam("page", "0")
                        .queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Audit log list fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].actionName").value("role.update"))
                .andExpect(jsonPath("$.data.content[0].oldData.code").value("EDITOR"));
    }
}
