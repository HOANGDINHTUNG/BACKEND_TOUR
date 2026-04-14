package com.wedservice.backend.module.reviews.service.command;

import com.wedservice.backend.module.reviews.dto.request.CreateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ModerateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ReplyReviewRequest;
import com.wedservice.backend.module.reviews.dto.response.ReviewResponse;

public interface ReviewCommandService {
    ReviewResponse createReview(CreateReviewRequest request);
    ReviewResponse replyReview(Long reviewId, ReplyReviewRequest request);
    ReviewResponse moderateReview(Long reviewId, ModerateReviewRequest request);
}
