package com.wedservice.backend.module.support.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.service.InternalNotificationService;
import com.wedservice.backend.module.support.dto.request.AssignSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.UpdateSupportSessionStatusRequest;
import com.wedservice.backend.module.support.dto.response.SupportMessageResponse;
import com.wedservice.backend.module.support.dto.response.SupportSessionResponse;
import com.wedservice.backend.module.support.entity.SupportMessage;
import com.wedservice.backend.module.support.entity.SupportSenderType;
import com.wedservice.backend.module.support.entity.SupportSession;
import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import com.wedservice.backend.module.support.repository.SupportMessageRepository;
import com.wedservice.backend.module.support.repository.SupportSessionRepository;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserCategory;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminSupportService {

    private final SupportSessionRepository supportSessionRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditTrailRecorder auditTrailRecorder;
    private final InternalNotificationService internalNotificationService;

    @Transactional(readOnly = true)
    public List<SupportSessionResponse> getSupportSessions(SupportSessionStatus status, UUID userId, UUID assignedStaffId) {
        Specification<SupportSession> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (assignedStaffId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignedStaffId"), assignedStaffId));
        }

        return supportSessionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(session -> toSessionResponse(session, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public SupportSessionResponse getSupportSession(Long id) {
        SupportSession session = findSupportSession(id);
        return toSessionResponse(session, supportMessageRepository.findBySessionIdOrderByCreatedAtAsc(id));
    }

    @Transactional
    public SupportSessionResponse assignSupportSession(Long id, AssignSupportSessionRequest request) {
        SupportSession session = findSupportSession(id);
        SupportSessionResponse oldState = toSessionResponse(session, null);
        UUID assignedStaffId = parseNullableUuid(request.getAssignedStaffId(), "assignedStaffId");
        if (assignedStaffId != null) {
            User staff = userRepository.findById(assignedStaffId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + assignedStaffId));
            if (staff.getDeletedAt() != null || staff.getStatus() != Status.ACTIVE || !isAssignableStaff(staff)) {
                throw new BadRequestException("assignedStaffId must reference an active internal staff user");
            }
        }
        session.setAssignedStaffId(assignedStaffId);
        SupportSession saved = supportSessionRepository.save(session);
        SupportSessionResponse response = toSessionResponse(saved, null);
        auditTrailRecorder.record(AuditActionType.SUPPORT_SESSION_ASSIGN, saved.getId(), oldState, response);
        return response;
    }

    @Transactional
    public SupportSessionResponse updateSupportSessionStatus(Long id, UpdateSupportSessionStatusRequest request) {
        SupportSession session = findSupportSession(id);
        SupportSessionResponse oldState = toSessionResponse(session, null);
        SupportSessionStatus status = request.getStatus();
        session.setStatus(status);
        if (status == SupportSessionStatus.RESOLVED || status == SupportSessionStatus.CLOSED) {
            session.setEndedAt(LocalDateTime.now());
        } else {
            session.setEndedAt(null);
        }
        SupportSession saved = supportSessionRepository.save(session);
        SupportSessionResponse response = toSessionResponse(saved, null);
        auditTrailRecorder.record(AuditActionType.SUPPORT_SESSION_STATUS_UPDATE, saved.getId(), oldState, response);
        return response;
    }

    @Transactional
    public SupportMessageResponse sendSupportReply(Long sessionId, CreateSupportMessageRequest request) {
        SupportSession session = findSupportSession(sessionId);
        ensureSessionCanReceiveMessage(session);

        UUID staffUserId = authenticatedUserProvider.getRequiredCurrentUserId();
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + staffUserId));
        if (staff.getDeletedAt() != null || staff.getStatus() != Status.ACTIVE || !isAssignableStaff(staff)) {
            throw new BadRequestException("Current user cannot reply to support sessions");
        }

        LocalDateTime now = LocalDateTime.now();
        SupportMessage savedMessage = supportMessageRepository.save(SupportMessage.builder()
                .sessionId(sessionId)
                .senderType(SupportSenderType.STAFF)
                .senderUserId(staffUserId)
                .messageText(normalizeRequiredMessage(request.getMessageText()))
                .attachmentUrl(normalizeNullable(request.getAttachmentUrl()))
                .build());

        if (session.getAssignedStaffId() == null) {
            session.setAssignedStaffId(staffUserId);
        }
        session.setStatus(SupportSessionStatus.WAITING_CUSTOMER);
        session.setEndedAt(null);
        session.setLastMessageAt(now);
        supportSessionRepository.save(session);

        // Notify customer
        internalNotificationService.sendInAppNotification(
                session.getUserId(),
                NotificationType.SUPPORT,
                "New Support Message",
                "A staff member has replied to your support session: " + session.getSessionCode(),
                "SUPPORT_SESSION",
                session.getId(),
                null
        );

        SupportMessageResponse response = toMessageResponse(savedMessage);
        auditTrailRecorder.record(AuditActionType.SUPPORT_MESSAGE_REPLY, savedMessage.getId(), null, response);
        return response;
    }

    private SupportSession findSupportSession(Long id) {
        return supportSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support session not found with id: " + id));
    }

    private boolean isAssignableStaff(User user) {
        return user.getUserCategory() == UserCategory.INTERNAL
                || user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getCode())
                .anyMatch(code -> "SUPER_ADMIN".equals(code)
                        || "ADMIN".equals(code)
                        || "OPERATOR".equals(code)
                        || "FIELD_STAFF".equals(code)
                        || "CONTENT_EDITOR".equals(code));
    }

    private void ensureSessionCanReceiveMessage(SupportSession session) {
        if (session.getStatus() == SupportSessionStatus.RESOLVED || session.getStatus() == SupportSessionStatus.CLOSED) {
            throw new BadRequestException("Support session is already closed");
        }
    }

    private UUID parseNullableUuid(String rawValue, String fieldName) {
        String normalized = normalizeNullable(rawValue);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        try {
            return UUID.fromString(normalized);
        } catch (Exception ex) {
            throw new BadRequestException(fieldName + " must be a valid UUID");
        }
    }

    private String normalizeRequiredMessage(String value) {
        String normalized = normalizeNullable(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException("messageText is required");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private SupportSessionResponse toSessionResponse(SupportSession session, List<SupportMessage> messages) {
        List<SupportMessageResponse> messageResponses = messages == null
                ? null
                : messages.stream().map(this::toMessageResponse).toList();
        int messageCount = messages != null
                ? messages.size()
                : supportMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).size();
        return SupportSessionResponse.builder()
                .id(session.getId())
                .sessionCode(session.getSessionCode())
                .userId(session.getUserId())
                .assignedStaffId(session.getAssignedStaffId())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .lastMessageAt(session.getLastMessageAt())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .rating(session.getRating())
                .feedback(session.getFeedback())
                .messageCount(messageCount)
                .messages(messageResponses)
                .build();
    }

    private SupportMessageResponse toMessageResponse(SupportMessage message) {
        return SupportMessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .senderType(message.getSenderType())
                .senderUserId(message.getSenderUserId())
                .messageText(message.getMessageText())
                .attachmentUrl(message.getAttachmentUrl())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
