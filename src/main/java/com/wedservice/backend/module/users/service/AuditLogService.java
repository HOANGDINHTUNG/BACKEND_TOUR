package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.users.entity.AuditLog;
import com.wedservice.backend.module.users.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    public void logChange(String actionName, String entityName, String entityId, Object oldData, Object newData) {
        AuditLog auditLog = AuditLog.builder()
                .actorUserId(resolveActorUserId())
                .actionName(actionName)
                .entityName(entityName)
                .entityId(entityId)
                .oldData(writeJson(oldData))
                .newData(writeJson(newData))
                .ipAddress(resolveIpAddress())
                .userAgent(resolveUserAgent())
                .build();
        auditLogRepository.save(auditLog);
    }

    public JsonNode parseJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readTree(rawJson);
        } catch (Exception ex) {
            return JsonNodeFactory.instance.textNode(rawJson);
        }
    }

    private UUID resolveActorUserId() {
        try {
            return authenticatedUserProvider.getRequiredCurrentUserId();
        } catch (Exception ex) {
            return null;
        }
    }

    private String resolveIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        return request == null ? null : request.getRemoteAddr();
    }

    private String resolveUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{\"serializationError\":true}";
        }
    }
}
