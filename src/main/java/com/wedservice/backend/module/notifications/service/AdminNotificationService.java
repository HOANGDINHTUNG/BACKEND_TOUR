package com.wedservice.backend.module.notifications.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.notifications.dto.request.AdminCreateNotificationRequest;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.entity.Notification;
import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.repository.NotificationRepository;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuditTrailRecorder auditTrailRecorder;
    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Transactional
    public NotificationResponse createNotification(AdminCreateNotificationRequest request) {
        UUID userId = parseUserId(request.getUserId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        NotificationChannel channel = request.getChannel() == null ? NotificationChannel.IN_APP : request.getChannel();
        if (channel != NotificationChannel.IN_APP) {
            throw new BadRequestException("Only in_app channel is supported in the current notification foundation");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getScheduledAt() != null && request.getScheduledAt().isAfter(now)) {
            throw new BadRequestException("Future scheduling is not supported yet");
        }

        String payload = normalizeNullable(request.getPayload());
        validatePayloadJson(payload);

        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(request.getNotificationType())
                .channel(channel)
                .title(normalizeRequiredText(request.getTitle(), "title"))
                .body(normalizeRequiredText(request.getBody(), "body"))
                .referenceType(normalizeNullable(request.getReferenceType()))
                .referenceId(request.getReferenceId())
                .payload(payload)
                .scheduledAt(request.getScheduledAt())
                .sentAt(now)
                .isBroadcast(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = toResponse(saved);
        auditTrailRecorder.record(AuditActionType.NOTIFICATION_CREATE, saved.getId(), null, response);
        return response;
    }

    private UUID parseUserId(String rawUserId) {
        try {
            return UUID.fromString(rawUserId);
        } catch (Exception ex) {
            throw new BadRequestException("userId must be a valid UUID");
        }
    }

    private void validatePayloadJson(String payload) {
        if (!StringUtils.hasText(payload)) {
            return;
        }
        try {
            jsonMapper.readTree(payload);
        } catch (Exception ex) {
            throw new BadRequestException("payload must be valid JSON");
        }
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .notificationType(notification.getNotificationType())
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .body(notification.getBody())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .payload(notification.getPayload())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .isBroadcast(notification.getIsBroadcast())
                .isRead(notification.getReadAt() != null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
