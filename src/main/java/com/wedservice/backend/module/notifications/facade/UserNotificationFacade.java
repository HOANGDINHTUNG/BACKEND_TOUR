package com.wedservice.backend.module.notifications.facade;

import com.wedservice.backend.module.notifications.dto.response.NotificationReadSummaryResponse;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserNotificationFacade {

    private final UserNotificationService userNotificationService;

    public List<NotificationResponse> getMyNotifications() {
        return userNotificationService.getMyNotifications();
    }

    public NotificationResponse markMyNotificationRead(Long id) {
        return userNotificationService.markMyNotificationRead(id);
    }

    public NotificationReadSummaryResponse markAllMyNotificationsRead() {
        return userNotificationService.markAllMyNotificationsRead();
    }
}
