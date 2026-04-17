package com.wedservice.backend.module.reviews.service.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.reviews.dto.request.CreateReviewRequest;
import com.wedservice.backend.module.reviews.dto.response.ReviewResponse;
import com.wedservice.backend.module.reviews.entity.Review;
import com.wedservice.backend.module.reviews.repository.ReviewAspectRepository;
import com.wedservice.backend.module.reviews.repository.ReviewReplyRepository;
import com.wedservice.backend.module.reviews.repository.ReviewRepository;
import com.wedservice.backend.module.reviews.validator.ReviewValidator;
import com.wedservice.backend.module.loyalty.service.MissionTrackerService;
import com.wedservice.backend.module.tours.service.TourRuntimeStatsSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewAspectRepository reviewAspectRepository;

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private ReviewValidator reviewValidator;

    @Mock
    private TourRuntimeStatsSyncService tourRuntimeStatsSyncService;
 
    @Mock
    private MissionTrackerService missionTrackerService;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(
                reviewRepository,
                reviewAspectRepository,
                reviewReplyRepository,
                bookingRepository,
                authenticatedUserProvider,
                reviewValidator,
                tourRuntimeStatsSyncService,
                missionTrackerService
        );
    }

    @Test
    void createReview_createsReviewForCompletedBooking() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(1L)
                .userId(userId)
                .tourId(10L)
                .scheduleId(20L)
                .contactName("Reviewer")
                .contactPhone("0909222222")
                .status(BookingStatus.COMPLETED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .build();

        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookingId(1L)
                .overallRating(5)
                .title("Great trip")
                .content("Everything went smoothly")
                .wouldRecommend(true)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);
        doNothing().when(reviewValidator).validateRating(5, "overallRating");
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(100L);
            return review;
        });
        when(reviewAspectRepository.findByReviewId(100L)).thenReturn(Collections.emptyList());
        when(reviewReplyRepository.findByReviewIdOrderByCreatedAtAsc(100L)).thenReturn(Collections.emptyList());

        ReviewResponse response = reviewService.createReview(request);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();

        assertThat(savedReview.getBookingId()).isEqualTo(1L);
        assertThat(savedReview.getUserId()).isEqualTo(userId);
        assertThat(savedReview.getTourId()).isEqualTo(10L);
        verify(tourRuntimeStatsSyncService).syncTourRating(10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBookingId()).isEqualTo(1L);
        assertThat(response.getOverallRating()).isEqualTo(5);
    }

    @Test
    void createReview_rejectsBookingThatIsNotCheckedInOrCompleted() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(2L)
                .userId(userId)
                .tourId(10L)
                .scheduleId(20L)
                .contactName("Reviewer")
                .contactPhone("0909333333")
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .build();

        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookingId(2L)
                .overallRating(4)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only checked-in or completed bookings can be reviewed");
    }
}
