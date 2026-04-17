package com.wedservice.backend.module.notifications.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.notifications.dto.request.AdminCreateNotificationRequest;
import com.wedservice.backend.module.notifications.entity.Notification;
import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.repository.NotificationRepository;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    @InjectMocks
    private AdminNotificationService adminNotificationService;

    @Test
    void createNotification_createsImmediateInAppNotification_andAudits() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
                .id(userId)
                .email("notify@example.com")
                .passwordHash("encoded")
                .fullName("Notify User")
                .status(Status.ACTIVE)
                .build()));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(10L);
            notification.setCreatedAt(LocalDateTime.of(2026, 4, 17, 12, 0));
            return notification;
        });

        AdminCreateNotificationRequest request = AdminCreateNotificationRequest.builder()
                .userId(userId.toString())
                .notificationType(NotificationType.PROMOTION)
                .title("  Promo moi  ")
                .body("  Voucher 20%  ")
                .payload("{\"voucherCode\":\"SPRING20\"}")
                .build();

        var response = adminNotificationService.createNotification(request);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification persisted = captor.getValue();
        assertThat(persisted.getUserId()).isEqualTo(userId);
        assertThat(persisted.getChannel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(persisted.getTitle()).isEqualTo("Promo moi");
        assertThat(persisted.getBody()).isEqualTo("Voucher 20%");
        assertThat(persisted.getPayload()).isEqualTo("{\"voucherCode\":\"SPRING20\"}");
        assertThat(persisted.getSentAt()).isNotNull();

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getNotificationType()).isEqualTo(NotificationType.PROMOTION);
        verify(auditTrailRecorder).record(eq(AuditActionType.NOTIFICATION_CREATE), eq(10L), eq(null), any());
    }

    @Test
    void createNotification_rejectsFutureScheduling() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
                .id(userId)
                .email("notify@example.com")
                .passwordHash("encoded")
                .fullName("Notify User")
                .status(Status.ACTIVE)
                .build()));

        AdminCreateNotificationRequest request = AdminCreateNotificationRequest.builder()
                .userId(userId.toString())
                .notificationType(NotificationType.SYSTEM)
                .channel(NotificationChannel.IN_APP)
                .title("Maintenance")
                .body("Body")
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();

        assertThatThrownBy(() -> adminNotificationService.createNotification(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Future scheduling is not supported yet");
    }

    @Test
    void createNotification_rejectsNonJsonPayload() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
                .id(userId)
                .email("notify@example.com")
                .passwordHash("encoded")
                .fullName("Notify User")
                .status(Status.ACTIVE)
                .build()));

        AdminCreateNotificationRequest request = AdminCreateNotificationRequest.builder()
                .userId(userId.toString())
                .notificationType(NotificationType.SYSTEM)
                .title("Maintenance")
                .body("Body")
                .payload("not-json")
                .build();

        assertThatThrownBy(() -> adminNotificationService.createNotification(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("payload must be valid JSON");
    }
}
