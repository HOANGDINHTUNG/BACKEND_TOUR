package com.wedservice.backend.module.engagement.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.engagement.dto.request.GenerateTourRecommendationRequest;
import com.wedservice.backend.module.engagement.dto.response.RecommendationLogResponse;
import com.wedservice.backend.module.engagement.facade.UserRecommendationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/recommendations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserRecommendationController {

    private final UserRecommendationFacade userRecommendationFacade;

    @PostMapping("/tours")
    public ApiResponse<RecommendationLogResponse> generateMyTourRecommendations(
            @Valid @RequestBody GenerateTourRecommendationRequest request
    ) {
        return ApiResponse.<RecommendationLogResponse>builder()
                .success(true)
                .message("Tour recommendations generated successfully")
                .data(userRecommendationFacade.generateMyTourRecommendations(request))
                .build();
    }

    @GetMapping("/logs")
    public ApiResponse<List<RecommendationLogResponse>> getMyRecommendationLogs() {
        return ApiResponse.<List<RecommendationLogResponse>>builder()
                .success(true)
                .message("Recommendation logs fetched successfully")
                .data(userRecommendationFacade.getMyRecommendationLogs())
                .build();
    }
}
