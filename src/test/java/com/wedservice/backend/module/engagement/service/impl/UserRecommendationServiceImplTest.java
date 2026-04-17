package com.wedservice.backend.module.engagement.service.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.engagement.dto.request.GenerateTourRecommendationRequest;
import com.wedservice.backend.module.engagement.dto.response.RecommendationLogResponse;
import com.wedservice.backend.module.engagement.entity.RecommendationLog;
import com.wedservice.backend.module.engagement.entity.UserTourView;
import com.wedservice.backend.module.engagement.entity.WishlistTour;
import com.wedservice.backend.module.engagement.repository.RecommendationLogRepository;
import com.wedservice.backend.module.engagement.repository.UserTourViewRepository;
import com.wedservice.backend.module.engagement.repository.WishlistTourRepository;
import com.wedservice.backend.module.tours.entity.Tag;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSeasonality;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TagRepository;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourSeasonalityRepository;
import com.wedservice.backend.module.tours.repository.TourTagRepository;
import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.entity.UserPreference;
import com.wedservice.backend.module.users.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRecommendationServiceImplTest {

    @Mock
    private RecommendationLogRepository recommendationLogRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private TourTagRepository tourTagRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TourSeasonalityRepository tourSeasonalityRepository;
    @Mock
    private WishlistTourRepository wishlistTourRepository;
    @Mock
    private UserTourViewRepository userTourViewRepository;
    @Mock
    private UserPreferenceRepository userPreferenceRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UserRecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserRecommendationServiceImpl(
                recommendationLogRepository,
                tourRepository,
                tourTagRepository,
                tagRepository,
                tourSeasonalityRepository,
                wishlistTourRepository,
                userTourViewRepository,
                userPreferenceRepository,
                authenticatedUserProvider
        );
    }

    @Test
    void generateMyTourRecommendations_scoresMatchingTourFirst_andPersistsLog() {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);

        UserPreference preference = UserPreference.builder()
                .userId(userId)
                .budgetLevel(BudgetLevel.MEDIUM)
                .preferredTripMode(PreferredTripMode.GROUP)
                .favoriteTags(List.of("beach"))
                .prefersFamilyFriendly(true)
                .build();
        when(userPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        Destination destination = Destination.builder().id(7L).build();
        Tour matchingTour = Tour.builder()
                .id(10L)
                .code("T-BEACH")
                .name("Beach Family Tour")
                .slug("beach-family-tour")
                .destination(destination)
                .basePrice(new BigDecimal("2200000"))
                .currency("VND")
                .durationDays(3)
                .durationNights(2)
                .shortDescription("Great beach")
                .isFeatured(true)
                .isFamilyFriendly(true)
                .averageRating(new BigDecimal("4.80"))
                .totalReviews(20)
                .totalBookings(40)
                .minGroupSize(1)
                .maxGroupSize(6)
                .tripMode("group")
                .difficultyLevel(2)
                .activityLevel(2)
                .status(TourStatus.ACTIVE)
                .build();
        Tour fallbackTour = Tour.builder()
                .id(11L)
                .code("T-CITY")
                .name("City Tour")
                .slug("city-tour")
                .destination(Destination.builder().id(8L).build())
                .basePrice(new BigDecimal("5000000"))
                .currency("VND")
                .durationDays(2)
                .durationNights(1)
                .averageRating(new BigDecimal("4.00"))
                .totalReviews(5)
                .totalBookings(5)
                .minGroupSize(1)
                .maxGroupSize(10)
                .tripMode("private")
                .difficultyLevel(3)
                .activityLevel(3)
                .status(TourStatus.ACTIVE)
                .build();

        when(tourRepository.findAll()).thenReturn(List.of(matchingTour, fallbackTour));
        when(tourTagRepository.findByIdTourIdIn(List.of(10L, 11L))).thenReturn(List.of(
                com.wedservice.backend.module.tours.entity.TourTag.builder()
                        .id(com.wedservice.backend.module.tours.entity.TourTagId.builder().tourId(10L).tagId(100L).build())
                        .build()
        ));
        when(tagRepository.findByIdInAndIsActiveTrue(any())).thenReturn(List.of(
                Tag.builder().id(100L).code("BEACH").name("Beach").tagGroup("nature").isActive(true).build()
        ));
        when(tourSeasonalityRepository.findByTourIdIn(List.of(10L, 11L))).thenReturn(List.of(
                TourSeasonality.builder().id(1L).tourId(10L).seasonName("Summer").monthFrom(5).monthTo(8).build()
        ));
        when(wishlistTourRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(
                WishlistTour.builder().tourId(10L).build()
        ));
        when(userTourViewRepository.findByUserIdOrderByViewedAtDesc(userId)).thenReturn(List.of(
                UserTourView.builder().tourId(10L).viewedAt(LocalDateTime.now()).build()
        ));
        when(tourRepository.findAllById(any())).thenReturn(List.of(matchingTour));
        when(recommendationLogRepository.save(any(RecommendationLog.class))).thenAnswer(invocation -> {
            RecommendationLog entity = invocation.getArgument(0);
            entity.setId(99L);
            entity.setCreatedAt(LocalDateTime.of(2026, 4, 17, 9, 0));
            return entity;
        });

        RecommendationLogResponse response = service.generateMyTourRecommendations(GenerateTourRecommendationRequest.builder()
                .requestedPeopleCount(4)
                .requestedDepartureAt(LocalDateTime.of(2026, 6, 20, 8, 0))
                .size(5)
                .build());

        assertThat(response.getLogId()).isEqualTo(99L);
        assertThat(response.getRecommendations()).hasSize(2);
        assertThat(response.getRecommendations().get(0).getTourId()).isEqualTo(10L);
        assertThat(response.getRecommendations().get(0).getScoringReasons())
                .contains("requested_tag_match", "trip_mode_match", "budget_match", "seasonality_match", "destination_affinity");

        ArgumentCaptor<RecommendationLog> logCaptor = ArgumentCaptor.forClass(RecommendationLog.class);
        verify(recommendationLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getRequestedTag()).isEqualTo("beach");
        assertThat(logCaptor.getValue().getRequestedBudget()).isEqualTo(BudgetLevel.MEDIUM);
        assertThat(logCaptor.getValue().getRequestedTripMode()).isEqualTo(PreferredTripMode.GROUP);
        assertThat(logCaptor.getValue().getGeneratedResult()).contains("T-BEACH");
        assertThat(logCaptor.getValue().getScoringDetail()).contains("phase4-personalization-v1");
    }

    @Test
    void getMyRecommendationLogs_returnsMostRecentFirst() {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(recommendationLogRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(
                RecommendationLog.builder()
                        .id(5L)
                        .userId(userId)
                        .requestedTag("beach")
                        .generatedResult("[{\"tourId\":10,\"tourCode\":\"T-BEACH\",\"tourName\":\"Beach Tour\",\"recommendationScore\":88.50,\"scoringReasons\":[\"requested_tag_match\"]}]")
                        .createdAt(LocalDateTime.of(2026, 4, 17, 9, 0))
                        .build()
        ));

        List<RecommendationLogResponse> responses = service.getMyRecommendationLogs();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getLogId()).isEqualTo(5L);
        assertThat(responses.get(0).getRecommendations()).hasSize(1);
        assertThat(responses.get(0).getRecommendations().get(0).getTourId()).isEqualTo(10L);
    }
}
