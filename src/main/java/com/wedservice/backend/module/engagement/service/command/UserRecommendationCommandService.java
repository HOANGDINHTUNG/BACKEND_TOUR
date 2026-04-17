package com.wedservice.backend.module.engagement.service.command;

import com.wedservice.backend.module.engagement.dto.request.GenerateTourRecommendationRequest;
import com.wedservice.backend.module.engagement.dto.response.RecommendationLogResponse;

public interface UserRecommendationCommandService {

    RecommendationLogResponse generateMyTourRecommendations(GenerateTourRecommendationRequest request);
}
