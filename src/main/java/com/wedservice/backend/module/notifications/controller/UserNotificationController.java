package com.wedservice.backend.module.notifications.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.notifications.dto.response.NotificationReadSummaryResponse;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.facade.UserNotificationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserNotificationController {

    private final UserNotificationFacade userNotificationFacade;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications fetched successfully")
                .data(userNotificationFacade.getMyNotifications())
                .build();
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markMyNotificationRead(@PathVariable Long id) {
        return ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification marked as read successfully")
                .data(userNotificationFacade.markMyNotificationRead(id))
                .build();
    }

    @PatchMapping("/read-all")
    public ApiResponse<NotificationReadSummaryResponse> markAllMyNotificationsRead() {
        return ApiResponse.<NotificationReadSummaryResponse>builder()
                .success(true)
                .message("Notifications marked as read successfully")
                .data(userNotificationFacade.markAllMyNotificationsRead())
                .build();
    }
}
