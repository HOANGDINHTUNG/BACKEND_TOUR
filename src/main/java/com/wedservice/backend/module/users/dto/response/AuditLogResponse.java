package com.wedservice.backend.module.users.dto.response;

import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private UUID actorUserId;
    private String actionName;
    private String entityName;
    private String entityId;
    private JsonNode oldData;
    private JsonNode newData;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
