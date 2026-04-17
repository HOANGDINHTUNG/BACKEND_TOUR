package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AuditLogSearchRequest;
import com.wedservice.backend.module.users.dto.response.AuditLogResponse;
import com.wedservice.backend.module.users.entity.AuditLog;
import com.wedservice.backend.module.users.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.node.JsonNodeFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuditLogQueryServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogService auditLogService;

    private AdminAuditLogQueryService adminAuditLogQueryService;

    @BeforeEach
    void setUp() {
        adminAuditLogQueryService = new AdminAuditLogQueryService(auditLogRepository, auditLogService);
    }

    @Test
    void getAuditLogs_returnsPagedAuditLogResponses() {
        UUID actorId = UUID.randomUUID();
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actorUserId(actorId)
                .actionName("role.update")
                .entityName("roles")
                .entityId("5")
                .oldData("{\"code\":\"EDITOR\"}")
                .newData("{\"code\":\"EDITOR_PLUS\"}")
                .ipAddress("127.0.0.1")
                .userAgent("JUnit")
                .createdAt(LocalDateTime.now())
                .build();
        AuditLogSearchRequest request = new AuditLogSearchRequest();
        request.setPage(0);
        request.setSize(20);

        when(auditLogRepository.findAll((Specification<AuditLog>) any(), eq(PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))))
                .thenReturn(new PageImpl<>(List.of(auditLog), PageRequest.of(0, 20), 1));
        when(auditLogService.parseJson("{\"code\":\"EDITOR\"}")).thenReturn(JsonNodeFactory.instance.objectNode().put("code", "EDITOR"));
        when(auditLogService.parseJson("{\"code\":\"EDITOR_PLUS\"}")).thenReturn(JsonNodeFactory.instance.objectNode().put("code", "EDITOR_PLUS"));

        PageResponse<AuditLogResponse> response = adminAuditLogQueryService.getAuditLogs(request);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getActionName()).isEqualTo("role.update");
        assertThat(response.getContent().get(0).getOldData().get("code").asString()).isEqualTo("EDITOR");
    }

    @Test
    void getAuditLogs_rejectsInvalidDateRange() {
        AuditLogSearchRequest request = new AuditLogSearchRequest();
        request.setFrom(LocalDateTime.of(2026, 4, 16, 12, 0));
        request.setTo(LocalDateTime.of(2026, 4, 15, 12, 0));

        assertThatThrownBy(() -> adminAuditLogQueryService.getAuditLogs(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("from must be before or equal to to");
    }
}
