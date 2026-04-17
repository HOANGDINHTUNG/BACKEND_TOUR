package com.wedservice.backend.module.notifications.service;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.notifications.entity.Notification;
import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UserNotificationService userNotificationService;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        userNotificationService = new UserNotificationService(notificationRepository, authenticatedUserProvider);
        currentUserId = UUID.randomUUID();
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
    }

    @Test
    void getMyNotifications_ordersUnreadFirstThenNewest() {
        Notification readOlder = notification(1L, LocalDateTime.of(2026, 4, 17, 9, 0), LocalDateTime.of(2026, 4, 17, 10, 0));
        Notification unreadNewer = notification(2L, LocalDateTime.of(2026, 4, 17, 11, 0), null);

        when(notificationRepository.findVisibleForUser(eq(currentUserId), any(LocalDateTime.class)))
                .thenReturn(List.of(readOlder, unreadNewer));

        var responses = userNotificationService.getMyNotifications();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(2L);
        assertThat(responses.get(0).getIsRead()).isFalse();
        assertThat(responses.get(1).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getIsRead()).isTrue();
    }

    @Test
    void markMyNotificationRead_setsReadAtWhenUnread() {
        Notification notification = notification(1L, LocalDateTime.of(2026, 4, 17, 11, 0), null);
        when(notificationRepository.findVisibleForUserById(eq(currentUserId), eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userNotificationService.markMyNotificationRead(1L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getReadAt()).isNotNull();
        assertThat(response.getIsRead()).isTrue();
    }

    @Test
    void markMyNotificationRead_isIdempotentWhenAlreadyRead() {
        Notification notification = notification(1L, LocalDateTime.of(2026, 4, 17, 11, 0), LocalDateTime.of(2026, 4, 17, 11, 30));
        when(notificationRepository.findVisibleForUserById(eq(currentUserId), eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(notification));

        var response = userNotificationService.markMyNotificationRead(1L);

        verify(notificationRepository, never()).save(any(Notification.class));
        assertThat(response.getReadAt()).isEqualTo(LocalDateTime.of(2026, 4, 17, 11, 30));
    }

    @Test
    void markMyNotificationRead_throwsWhenNotVisible() {
        when(notificationRepository.findVisibleForUserById(eq(currentUserId), eq(99L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userNotificationService.markMyNotificationRead(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with id: 99");
    }

    @Test
    void markAllMyNotificationsRead_updatesOnlyUnreadRows() {
        Notification unreadA = notification(1L, LocalDateTime.of(2026, 4, 17, 9, 0), null);
        Notification unreadB = notification(2L, LocalDateTime.of(2026, 4, 17, 10, 0), null);
        Notification read = notification(3L, LocalDateTime.of(2026, 4, 17, 11, 0), LocalDateTime.of(2026, 4, 17, 11, 5));

        when(notificationRepository.findVisibleForUser(eq(currentUserId), any(LocalDateTime.class)))
                .thenReturn(List.of(unreadA, unreadB, read));

        var response = userNotificationService.markAllMyNotificationsRead();

        verify(notificationRepository).saveAll(List.of(unreadA, unreadB));
        assertThat(response.getUpdatedCount()).isEqualTo(2);
        assertThat(unreadA.getReadAt()).isNotNull();
        assertThat(unreadB.getReadAt()).isNotNull();
    }

    private Notification notification(Long id, LocalDateTime sentAt, LocalDateTime readAt) {
        return Notification.builder()
                .id(id)
                .userId(currentUserId)
                .notificationType(NotificationType.SYSTEM)
                .channel(NotificationChannel.IN_APP)
                .title("Notice " + id)
                .body("Body " + id)
                .sentAt(sentAt)
                .readAt(readAt)
                .isBroadcast(false)
                .createdAt(sentAt.minusMinutes(5))
                .build();
    }
}
