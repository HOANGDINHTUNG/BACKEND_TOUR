package com.wedservice.backend.module.support.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.RateSupportSessionRequest;
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
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSupportService {

    private final SupportSessionRepository supportSessionRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserRepository userRepository;

    @Transactional
    public SupportSessionResponse createMySupportSession(CreateSupportSessionRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        ensureSupportEligibleUser(userId);

        LocalDateTime now = LocalDateTime.now();
        SupportSession session = supportSessionRepository.save(SupportSession.builder()
                .sessionCode("CS" + System.currentTimeMillis())
                .userId(userId)
                .status(SupportSessionStatus.OPEN)
                .startedAt(now)
                .lastMessageAt(now)
                .build());

        SupportMessage message = supportMessageRepository.save(SupportMessage.builder()
                .sessionId(session.getId())
                .senderType(SupportSenderType.CUSTOMER)
                .senderUserId(userId)
                .messageText(normalizeRequiredMessage(request.getInitialMessage()))
                .attachmentUrl(normalizeNullable(request.getAttachmentUrl()))
                .build());

        return toSessionResponse(session, List.of(message));
    }

    @Transactional(readOnly = true)
    public List<SupportSessionResponse> getMySupportSessions() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        return supportSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(session -> toSessionResponse(session, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public SupportSessionResponse getMySupportSession(Long id) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        SupportSession session = supportSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Support session not found with id: " + id));
        return toSessionResponse(session, supportMessageRepository.findBySessionIdOrderByCreatedAtAsc(id));
    }

    @Transactional
    public SupportMessageResponse sendMySupportMessage(Long sessionId, CreateSupportMessageRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        SupportSession session = supportSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Support session not found with id: " + sessionId));
        ensureSessionCanReceiveMessage(session);

        LocalDateTime now = LocalDateTime.now();
        SupportMessage savedMessage = supportMessageRepository.save(SupportMessage.builder()
                .sessionId(sessionId)
                .senderType(SupportSenderType.CUSTOMER)
                .senderUserId(userId)
                .messageText(normalizeRequiredMessage(request.getMessageText()))
                .attachmentUrl(normalizeNullable(request.getAttachmentUrl()))
                .build());

        session.setStatus(SupportSessionStatus.WAITING_STAFF);
        session.setEndedAt(null);
        session.setLastMessageAt(now);
        supportSessionRepository.save(session);

        return toMessageResponse(savedMessage);
    }

    @Transactional
    public SupportSessionResponse rateMySupportSession(Long sessionId, RateSupportSessionRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        SupportSession session = supportSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Support session not found with id: " + sessionId));

        if (session.getStatus() != SupportSessionStatus.RESOLVED && session.getStatus() != SupportSessionStatus.CLOSED) {
            throw new BadRequestException("Only resolved or closed sessions can be rated");
        }

        session.setRating(request.getRating());
        session.setFeedback(normalizeNullable(request.getFeedback()));
        SupportSession saved = supportSessionRepository.save(session);

        return toSessionResponse(saved, supportMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId));
    }

    private void ensureSupportEligibleUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getDeletedAt() != null || user.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Current user cannot create support sessions");
        }
    }

    private void ensureSessionCanReceiveMessage(SupportSession session) {
        if (session.getStatus() == SupportSessionStatus.RESOLVED || session.getStatus() == SupportSessionStatus.CLOSED) {
            throw new BadRequestException("Support session is already closed");
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
