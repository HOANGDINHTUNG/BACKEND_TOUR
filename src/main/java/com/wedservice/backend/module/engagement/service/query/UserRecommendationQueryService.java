package com.wedservice.backend.module.engagement.service.query;

import com.wedservice.backend.module.engagement.dto.response.RecommendationLogResponse;

import java.util.List;

public interface UserRecommendationQueryService {

    List<RecommendationLogResponse> getMyRecommendationLogs();
}
