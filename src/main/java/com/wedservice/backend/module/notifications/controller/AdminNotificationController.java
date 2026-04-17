package com.wedservice.backend.module.notifications.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.notifications.dto.request.AdminCreateNotificationRequest;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.facade.AdminNotificationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationFacade adminNotificationFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('user.update')")
    public ApiResponse<NotificationResponse> createNotification(@Valid @RequestBody AdminCreateNotificationRequest request) {
        return ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification created successfully")
                .data(adminNotificationFacade.createNotification(request))
                .build();
    }
}
