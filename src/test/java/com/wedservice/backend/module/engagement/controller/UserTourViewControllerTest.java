package com.wedservice.backend.module.engagement.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.engagement.dto.response.ViewedTourResponse;
import com.wedservice.backend.module.engagement.facade.UserTourViewFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserTourViewControllerTest {

    @Mock
    private UserTourViewFacade userTourViewFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserTourViewController(userTourViewFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyTourViews_returnsWrappedApiResponse() throws Exception {
        ViewedTourResponse response = ViewedTourResponse.builder()
                .viewId(1L)
                .tourId(10L)
                .tourCode("TOUR-10")
                .tourName("Viewed Tour")
                .tourSlug("viewed-tour")
                .basePrice(new BigDecimal("1300000"))
                .currency("VND")
                .durationDays(2)
                .durationNights(1)
                .viewedAt(LocalDateTime.of(2026, 4, 17, 11, 0))
                .build();

        when(userTourViewFacade.getMyTourViews()).thenReturn(List.of(response));

        mockMvc.perform(get("/users/me/tour-views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Viewed tours fetched successfully"))
                .andExpect(jsonPath("$.data[0].tourId").value(10L));
    }
}
