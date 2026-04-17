package com.wedservice.backend.module.engagement.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.engagement.dto.request.GenerateTourRecommendationRequest;
import com.wedservice.backend.module.engagement.dto.response.RecommendationLogResponse;
import com.wedservice.backend.module.engagement.dto.response.RecommendedTourResponse;
import com.wedservice.backend.module.engagement.facade.UserRecommendationFacade;
import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRecommendationControllerTest {

    @Mock
    private UserRecommendationFacade userRecommendationFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserRecommendationController(userRecommendationFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void generateMyTourRecommendations_returnsWrappedApiResponse() throws Exception {
        GenerateTourRecommendationRequest request = GenerateTourRecommendationRequest.builder()
                .requestedTag("beach")
                .requestedBudget(BudgetLevel.MEDIUM)
                .requestedTripMode(PreferredTripMode.GROUP)
                .requestedPeopleCount(4)
                .requestedDepartureAt(LocalDateTime.of(2026, 6, 20, 8, 0))
                .size(5)
                .build();

        RecommendationLogResponse response = RecommendationLogResponse.builder()
                .logId(12L)
                .requestedTag("beach")
                .recommendations(List.of(RecommendedTourResponse.builder()
                        .tourId(10L)
                        .tourCode("T-BEACH")
                        .tourName("Beach Tour")
                        .recommendationScore(new BigDecimal("88.50"))
                        .build()))
                .createdAt(LocalDateTime.of(2026, 4, 17, 9, 0))
                .build();

        when(userRecommendationFacade.generateMyTourRecommendations(any(GenerateTourRecommendationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users/me/recommendations/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tour recommendations generated successfully"))
                .andExpect(jsonPath("$.data.logId").value(12L))
                .andExpect(jsonPath("$.data.recommendations[0].tourId").value(10L));
    }

    @Test
    void getMyRecommendationLogs_returnsWrappedApiResponse() throws Exception {
        when(userRecommendationFacade.getMyRecommendationLogs()).thenReturn(List.of(
                RecommendationLogResponse.builder()
                        .logId(12L)
                        .requestedTag("beach")
                        .createdAt(LocalDateTime.of(2026, 4, 17, 9, 0))
                        .recommendations(List.of())
                        .build()
        ));

        mockMvc.perform(get("/users/me/recommendations/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recommendation logs fetched successfully"))
                .andExpect(jsonPath("$.data[0].logId").value(12L))
                .andExpect(jsonPath("$.data[0].requestedTag").value("beach"));
    }
}
