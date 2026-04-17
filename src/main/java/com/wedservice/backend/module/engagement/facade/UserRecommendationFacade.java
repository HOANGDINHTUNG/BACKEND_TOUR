package com.wedservice.backend.module.engagement.facade;

import com.wedservice.backend.module.engagement.dto.request.GenerateTourRecommendationRequest;
import com.wedservice.backend.module.engagement.dto.response.RecommendationLogResponse;
import com.wedservice.backend.module.engagement.service.command.UserRecommendationCommandService;
import com.wedservice.backend.module.engagement.service.query.UserRecommendationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRecommendationFacade {

    private final UserRecommendationCommandService commandService;
    private final UserRecommendationQueryService queryService;

    public RecommendationLogResponse generateMyTourRecommendations(GenerateTourRecommendationRequest request) {
        return commandService.generateMyTourRecommendations(request);
    }

    public List<RecommendationLogResponse> getMyRecommendationLogs() {
        return queryService.getMyRecommendationLogs();
    }
}
