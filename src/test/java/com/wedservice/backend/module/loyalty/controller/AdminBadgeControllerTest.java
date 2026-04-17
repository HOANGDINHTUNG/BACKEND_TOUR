package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.loyalty.dto.request.AdminBadgeRequest;
import com.wedservice.backend.module.loyalty.dto.response.BadgeResponse;
import com.wedservice.backend.module.loyalty.dto.response.PassportBadgeResponse;
import com.wedservice.backend.module.loyalty.facade.AdminBadgeFacade;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminBadgeControllerTest {

    @Mock
    private AdminBadgeFacade adminBadgeFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminBadgeController(adminBadgeFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getBadges_returnsWrappedApiResponse() throws Exception {
        when(adminBadgeFacade.getBadges()).thenReturn(List.of(
                BadgeResponse.builder()
                        .id(1L)
                        .code("FIRST_TRIP")
                        .name("First Trip")
                        .isActive(true)
                        .build()
        ));

        mockMvc.perform(get("/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Badges fetched successfully"))
                .andExpect(jsonPath("$.data[0].code").value("FIRST_TRIP"));
    }

    @Test
    void createBadge_returnsWrappedApiResponse() throws Exception {
        when(adminBadgeFacade.createBadge(any(AdminBadgeRequest.class))).thenReturn(
                BadgeResponse.builder()
                        .id(2L)
                        .code("CHECKIN_MASTER")
                        .name("Checkin Master")
                        .isActive(true)
                        .build()
        );

        mockMvc.perform(post("/badges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AdminBadgeRequest.builder()
                                .code("CHECKIN_MASTER")
                                .name("Checkin Master")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Badge created successfully"))
                .andExpect(jsonPath("$.data.id").value(2L));
    }

    @Test
    void grantBadge_returnsWrappedApiResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(adminBadgeFacade.grantBadge(eq(userId), eq(9L))).thenReturn(
                PassportBadgeResponse.builder()
                        .passportBadgeId(5L)
                        .badgeId(9L)
                        .badgeCode("FIRST_TRIP")
                        .build()
        );

        mockMvc.perform(post("/badges/9/grant/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Badge granted successfully"))
                .andExpect(jsonPath("$.data.badgeCode").value("FIRST_TRIP"));
    }
}
