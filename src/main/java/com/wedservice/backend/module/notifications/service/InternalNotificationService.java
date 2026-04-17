package com.wedservice.backend.module.notifications.service;

import com.wedservice.backend.module.notifications.entity.Notification;
import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendInAppNotification(
            UUID userId,
            NotificationType type,
            String title,
            String body,
            String referenceType,
            Long referenceId,
            String payload
    ) {
        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(type)
                .channel(NotificationChannel.IN_APP)
                .title(title)
                .body(body)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .payload(payload)
                .sentAt(LocalDateTime.now())
                .isBroadcast(false)
                .build();

        notificationRepository.save(notification);
    }
}
