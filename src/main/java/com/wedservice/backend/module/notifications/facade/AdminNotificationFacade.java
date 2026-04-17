package com.wedservice.backend.module.notifications.facade;

import com.wedservice.backend.module.notifications.dto.request.AdminCreateNotificationRequest;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminNotificationFacade {

    private final AdminNotificationService adminNotificationService;

    public NotificationResponse createNotification(AdminCreateNotificationRequest request) {
        return adminNotificationService.createNotification(request);
    }
}
