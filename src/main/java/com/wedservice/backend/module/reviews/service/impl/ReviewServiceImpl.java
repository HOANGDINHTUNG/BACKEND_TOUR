package com.wedservice.backend.module.reviews.service.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.reviews.dto.request.CreateReviewAspectRequest;
import com.wedservice.backend.module.reviews.dto.request.CreateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ModerateReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ReplyReviewRequest;
import com.wedservice.backend.module.reviews.dto.request.ReviewSearchRequest;
import com.wedservice.backend.module.reviews.dto.response.ReviewAspectResponse;
import com.wedservice.backend.module.reviews.dto.response.ReviewReplyResponse;
import com.wedservice.backend.module.reviews.dto.response.ReviewResponse;
import com.wedservice.backend.module.reviews.entity.Review;
import com.wedservice.backend.module.reviews.entity.ReviewAspect;
import com.wedservice.backend.module.reviews.entity.ReviewReply;
import com.wedservice.backend.module.reviews.entity.ReviewSentiment;
import com.wedservice.backend.module.reviews.repository.ReviewAspectRepository;
import com.wedservice.backend.module.reviews.repository.ReviewReplyRepository;
import com.wedservice.backend.module.reviews.repository.ReviewRepository;
import com.wedservice.backend.module.reviews.service.command.ReviewCommandService;
import com.wedservice.backend.module.reviews.service.query.ReviewQueryService;
import com.wedservice.backend.module.reviews.validator.ReviewValidator;
import com.wedservice.backend.module.tours.service.TourRuntimeStatsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewCommandService, ReviewQueryService {

    private static final Set<BookingStatus> ALLOWED_REVIEW_BOOKING_STATUS = Set.of(
            BookingStatus.CHECKED_IN,
            BookingStatus.COMPLETED
    );

    private final ReviewRepository reviewRepository;
    private final ReviewAspectRepository reviewAspectRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ReviewValidator reviewValidator;
    private final TourRuntimeStatsSyncService tourRuntimeStatsSyncService;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        UUID currentUserId = authenticatedUserProvider.getRequiredCurrentUserId();
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if (!authenticatedUserProvider.isCurrentUserBackoffice() && !currentUserId.equals(booking.getUserId())) {
            throw new AccessDeniedException("You do not have permission to review this booking");
        }
        if (!ALLOWED_REVIEW_BOOKING_STATUS.contains(booking.getStatus())) {
            throw new BadRequestException("Only checked-in or completed bookings can be reviewed");
        }
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("Review for this booking already exists");
        }

        reviewValidator.validateRating(request.getOverallRating(), "overallRating");

        Review review = Review.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .tourId(booking.getTourId())
                .scheduleId(booking.getScheduleId())
                .overallRating(request.getOverallRating())
                .title(request.getTitle())
                .content(request.getContent())
                .sentiment(ReviewSentiment.NEUTRAL.getValue())
                .wouldRecommend(request.getWouldRecommend() == null ? true : request.getWouldRecommend())
                .build();
        review = reviewRepository.save(review);

        saveAspects(review.getId(), request.getAspects());
        tourRuntimeStatsSyncService.syncTourRating(review.getTourId());
        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getTourReviews(Long tourId, ReviewSearchRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<Review> page = reviewRepository.findByTourId(tourId, pageRequest);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMyReviews(ReviewSearchRequest request) {
        UUID currentUserId = authenticatedUserProvider.getRequiredCurrentUserId();
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<Review> page = reviewRepository.findByUserId(currentUserId, pageRequest);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional
    public ReviewResponse replyReview(Long reviewId, ReplyReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        ReviewReply reply = ReviewReply.builder()
                .reviewId(review.getId())
                .staffId(authenticatedUserProvider.getRequiredCurrentUserId())
                .content(request.getContent())
                .build();
        reviewReplyRepository.save(reply);

        return toResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse moderateReview(Long reviewId, ModerateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        review.setSentiment(reviewValidator.normalizeSentiment(request.getSentiment()));
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    private void saveAspects(Long reviewId, List<CreateReviewAspectRequest> aspects) {
        if (aspects == null || aspects.isEmpty()) {
            return;
        }
        Set<String> uniqueAspectNames = new HashSet<>();
        for (CreateReviewAspectRequest aspect : aspects) {
            if (aspect == null) {
                continue;
            }
            reviewValidator.validateRating(aspect.getAspectRating(), "aspectRating");
            String normalizedAspectName = aspect.getAspectName() == null ? "" : aspect.getAspectName().trim().toLowerCase();
            if (!uniqueAspectNames.add(normalizedAspectName)) {
                throw new BadRequestException("Duplicate aspect name: " + aspect.getAspectName());
            }
            ReviewAspect entity = ReviewAspect.builder()
                    .reviewId(reviewId)
                    .aspectName(aspect.getAspectName() == null ? null : aspect.getAspectName().trim())
                    .aspectRating(aspect.getAspectRating())
                    .comment(aspect.getComment())
                    .build();
            reviewAspectRepository.save(entity);
        }
    }

    private ReviewResponse toResponse(Review review) {
        List<ReviewAspectResponse> aspects = reviewAspectRepository.findByReviewId(review.getId()).stream()
                .map(a -> ReviewAspectResponse.builder()
                        .id(a.getId())
                        .aspectName(a.getAspectName())
                        .aspectRating(a.getAspectRating())
                        .comment(a.getComment())
                        .build())
                .toList();

        List<ReviewReplyResponse> replies = reviewReplyRepository.findByReviewIdOrderByCreatedAtAsc(review.getId()).stream()
                .map(r -> ReviewReplyResponse.builder()
                        .id(r.getId())
                        .staffId(r.getStaffId())
                        .content(r.getContent())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBookingId())
                .userId(review.getUserId())
                .tourId(review.getTourId())
                .scheduleId(review.getScheduleId())
                .overallRating(review.getOverallRating())
                .title(review.getTitle())
                .content(review.getContent())
                .sentiment(review.getSentiment())
                .wouldRecommend(review.getWouldRecommend())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .aspects(aspects)
                .replies(replies)
                .build();
    }
}
