package com.wedservice.backend.module.notifications.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.notifications.dto.response.NotificationReadSummaryResponse;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.facade.UserNotificationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserNotificationControllerTest {

    @Mock
    private UserNotificationFacade userNotificationFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserNotificationController(userNotificationFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyNotifications_returnsWrappedApiResponse() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(UUID.randomUUID())
                .notificationType(NotificationType.SYSTEM)
                .channel(NotificationChannel.IN_APP)
                .title("He thong")
                .body("Bao tri")
                .isRead(false)
                .createdAt(LocalDateTime.of(2026, 4, 17, 14, 0))
                .build();

        when(userNotificationFacade.getMyNotifications()).thenReturn(List.of(response));

        mockMvc.perform(get("/users/me/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notifications fetched successfully"))
                .andExpect(jsonPath("$.data[0].title").value("He thong"));
    }

    @Test
    void markMyNotificationRead_returnsWrappedApiResponse() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .notificationType(NotificationType.SYSTEM)
                .channel(NotificationChannel.IN_APP)
                .title("He thong")
                .body("Bao tri")
                .isRead(true)
                .readAt(LocalDateTime.of(2026, 4, 17, 14, 5))
                .build();

        when(userNotificationFacade.markMyNotificationRead(1L)).thenReturn(response);

        mockMvc.perform(patch("/users/me/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification marked as read successfully"))
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    void markAllMyNotificationsRead_returnsWrappedApiResponse() throws Exception {
        when(userNotificationFacade.markAllMyNotificationsRead()).thenReturn(
                NotificationReadSummaryResponse.builder()
                        .updatedCount(3)
                        .build()
        );

        mockMvc.perform(patch("/users/me/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notifications marked as read successfully"))
                .andExpect(jsonPath("$.data.updatedCount").value(3));
    }
}
