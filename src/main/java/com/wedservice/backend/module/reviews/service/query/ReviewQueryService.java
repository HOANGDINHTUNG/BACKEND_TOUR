package com.wedservice.backend.module.reviews.service.query;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.reviews.dto.request.ReviewSearchRequest;
import com.wedservice.backend.module.reviews.dto.response.ReviewResponse;

public interface ReviewQueryService {
    ReviewResponse getReview(Long id);
    PageResponse<ReviewResponse> getTourReviews(Long tourId, ReviewSearchRequest request);
    PageResponse<ReviewResponse> getMyReviews(ReviewSearchRequest request);
}
