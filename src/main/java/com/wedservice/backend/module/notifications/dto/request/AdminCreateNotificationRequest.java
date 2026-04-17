package com.wedservice.backend.module.notifications.dto.request;

import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateNotificationRequest {

    @NotBlank
    private String userId;

    @NotNull
    private NotificationType notificationType;

    private NotificationChannel channel;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String body;

    @Size(max = 80)
    private String referenceType;

    private Long referenceId;

    private String payload;

    private LocalDateTime scheduledAt;
}
