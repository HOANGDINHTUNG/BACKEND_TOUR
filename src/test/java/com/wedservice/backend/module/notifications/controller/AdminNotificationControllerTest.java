package com.wedservice.backend.module.notifications.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.notifications.dto.request.AdminCreateNotificationRequest;
import com.wedservice.backend.module.notifications.dto.response.NotificationResponse;
import com.wedservice.backend.module.notifications.entity.NotificationChannel;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.facade.AdminNotificationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminNotificationControllerTest {

    @Mock
    private AdminNotificationFacade adminNotificationFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminNotificationController(adminNotificationFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createNotification_returnsWrappedApiResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        AdminCreateNotificationRequest request = AdminCreateNotificationRequest.builder()
                .userId(userId.toString())
                .notificationType(NotificationType.PROMOTION)
                .channel(NotificationChannel.IN_APP)
                .title("Promo moi")
                .body("Voucher 20%")
                .payload("{\"voucherCode\":\"SPRING20\"}")
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(10L)
                .userId(userId)
                .notificationType(NotificationType.PROMOTION)
                .channel(NotificationChannel.IN_APP)
                .title("Promo moi")
                .body("Voucher 20%")
                .payload("{\"voucherCode\":\"SPRING20\"}")
                .isRead(false)
                .createdAt(LocalDateTime.of(2026, 4, 17, 13, 0))
                .build();

        when(adminNotificationFacade.createNotification(any(AdminCreateNotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification created successfully"))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.notificationType").value("PROMOTION"));
    }
}
