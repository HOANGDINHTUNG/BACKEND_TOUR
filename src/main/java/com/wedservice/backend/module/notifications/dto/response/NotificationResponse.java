package com.wedservice.backend.module.notifications.dto.response;

import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private Long id;
    private UUID userId;
    private NotificationType notificationType;
    private NotificationChannel channel;
    private String title;
    private String body;
    private String referenceType;
    private Long referenceId;
    private String payload;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private Boolean isBroadcast;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
