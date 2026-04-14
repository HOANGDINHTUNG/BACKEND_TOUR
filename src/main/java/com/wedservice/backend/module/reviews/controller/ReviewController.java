package com.wedservice.backend.module.reviews.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.reviews.dto.request.CreateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ModerateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ReplyReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ReviewSearchRequest;
import com.wedservice.backend.module.reviews.dto.response.ReviewResponse;
import com.wedservice.backend.module.reviews.facade.ReviewFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewFacade reviewFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('review.create')")
    public ApiResponse<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewFacade.createReview(request), "Review created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('review.view')")
    public ApiResponse<ReviewResponse> getReview(@PathVariable Long id) {
        return ApiResponse.success(reviewFacade.getReview(id));
    }

    @GetMapping("/tours/{tourId}")
    @PreAuthorize("hasAuthority('review.view')")
    public ApiResponse<PageResponse<ReviewResponse>> getTourReviews(
            @PathVariable Long tourId,
            @Valid @ModelAttribute ReviewSearchRequest request
    ) {
        return ApiResponse.success(reviewFacade.getTourReviews(tourId, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('review.view')")
    public ApiResponse<PageResponse<ReviewResponse>> getMyReviews(@Valid @ModelAttribute ReviewSearchRequest request) {
        return ApiResponse.success(reviewFacade.getMyReviews(request));
    }

    @PostMapping("/{id}/replies")
    @PreAuthorize("hasAuthority('review.reply')")
    public ApiResponse<ReviewResponse> replyReview(
            @PathVariable Long id,
            @Valid @RequestBody ReplyReviewRequest request
    ) {
        return ApiResponse.success(reviewFacade.replyReview(id, request), "Review replied successfully");
    }

    @PatchMapping("/{id}/moderation")
    @PreAuthorize("hasAuthority('review.moderate')")
    public ApiResponse<ReviewResponse> moderateReview(
            @PathVariable Long id,
            @Valid @RequestBody ModerateReviewRequest request
    ) {
        return ApiResponse.success(reviewFacade.moderateReview(id, request), "Review moderated successfully");
    }
}
