package com.wedservice.backend.module.reviews.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.reviews.dto.request.CreateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ModerateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ReplyReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ReviewSearchRequest;
import com.wedservice.backend.module.reviews.dto.response.ReviewResponse;
import com.wedservice.backend.module.reviews.service.command.ReviewCommandService;
import com.wedservice.backend.module.reviews.service.query.ReviewQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    public ReviewResponse createReview(CreateReviewRequest request) {
        return reviewCommandService.createReview(request);
    }

    public ReviewResponse getReview(Long id) {
        return reviewQueryService.getReview(id);
    }

    public PageResponse<ReviewResponse> getTourReviews(Long tourId, ReviewSearchRequest request) {
        return reviewQueryService.getTourReviews(tourId, request);
    }

    public PageResponse<ReviewResponse> getMyReviews(ReviewSearchRequest request) {
        return reviewQueryService.getMyReviews(request);
    }

    public ReviewResponse replyReview(Long reviewId, ReplyReviewRequest request) {
        return reviewCommandService.replyReview(reviewId, request);
    }

    public ReviewResponse moderateReview(Long reviewId, ModerateReviewRequest request) {
        return reviewCommandService.moderateReview(reviewId, request);
    }
}
