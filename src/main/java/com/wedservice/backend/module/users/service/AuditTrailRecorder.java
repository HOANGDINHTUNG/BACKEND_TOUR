package com.wedservice.backend.module.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditTrailRecorder {

    private final AuditLogService auditLogService;

    public void record(AuditActionType actionType, Object entityId, Object oldData, Object newData) {
        auditLogService.logChange(
                actionType.getActionName(),
                actionType.getEntityName(),
                String.valueOf(entityId),
                oldData,
                newData
        );
    }
}
