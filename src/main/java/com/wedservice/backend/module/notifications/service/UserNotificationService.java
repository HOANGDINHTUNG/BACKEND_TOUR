package com.wedservice.backend.module.notifications.service;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.notifications.dto.response.NotificationReadSummaryResponse;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.entity.Notification;
import com.wedservice.backend.module.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        return notificationRepository.findVisibleForUser(userId, now).stream()
                .sorted(Comparator
                        .comparing((Notification notification) -> notification.getReadAt() != null)
                        .thenComparing(Notification::getSentAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Notification::getCreatedAt, Comparator.reverseOrder()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markMyNotificationRead(Long id) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        Notification notification = notificationRepository.findVisibleForUserById(userId, id, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Transactional
    public NotificationReadSummaryResponse markAllMyNotificationsRead() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        List<Notification> notifications = notificationRepository.findVisibleForUser(userId, now).stream()
                .filter(notification -> notification.getReadAt() == null)
                .toList();

        notifications.forEach(notification -> notification.setReadAt(now));
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }

        return NotificationReadSummaryResponse.builder()
                .updatedCount(notifications.size())
                .build();
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
