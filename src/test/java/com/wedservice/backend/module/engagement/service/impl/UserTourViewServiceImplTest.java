package com.wedservice.backend.module.engagement.service.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.engagement.entity.UserTourView;
import com.wedservice.backend.module.engagement.repository.UserTourViewRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.users.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTourViewServiceImplTest {

    @Mock
    private UserTourViewRepository userTourViewRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UserTourViewServiceImpl service;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        service = new UserTourViewServiceImpl(userTourViewRepository, tourRepository, authenticatedUserProvider);
        currentUserId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recordCurrentUserTourViewIfAuthenticated_savesViewForAuthenticatedUser() {
        setAuthenticatedUser(currentUserId);
        when(tourRepository.findById(10L)).thenReturn(Optional.of(activeTour(10L)));
        when(userTourViewRepository.findTopByUserIdAndTourIdOrderByViewedAtDesc(currentUserId, 10L)).thenReturn(Optional.empty());

        service.recordCurrentUserTourViewIfAuthenticated(10L);

        ArgumentCaptor<UserTourView> captor = ArgumentCaptor.forClass(UserTourView.class);
        verify(userTourViewRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(currentUserId);
        assertThat(captor.getValue().getTourId()).isEqualTo(10L);
        assertThat(captor.getValue().getViewedAt()).isNotNull();
    }

    @Test
    void recordCurrentUserTourViewIfAuthenticated_skipsAnonymousRequests() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
        );

        service.recordCurrentUserTourViewIfAuthenticated(10L);

        verify(userTourViewRepository, never()).save(any(UserTourView.class));
    }

    @Test
    void recordCurrentUserTourViewIfAuthenticated_skipsWhenViewIsWithinCooldown() {
        setAuthenticatedUser(currentUserId);
        when(tourRepository.findById(10L)).thenReturn(Optional.of(activeTour(10L)));
        when(userTourViewRepository.findTopByUserIdAndTourIdOrderByViewedAtDesc(currentUserId, 10L))
                .thenReturn(Optional.of(UserTourView.builder()
                        .id(1L)
                        .userId(currentUserId)
                        .tourId(10L)
                        .viewedAt(LocalDateTime.now().minusMinutes(5))
                        .build()));

        service.recordCurrentUserTourViewIfAuthenticated(10L);

        verify(userTourViewRepository, never()).save(any(UserTourView.class));
    }

    @Test
    void getMyTourViews_returnsLatestUniqueActiveTours() {
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);

        UserTourView latestTour10 = UserTourView.builder()
                .id(5L)
                .userId(currentUserId)
                .tourId(10L)
                .viewedAt(LocalDateTime.of(2026, 4, 17, 10, 0))
                .build();
        UserTourView olderTour10 = UserTourView.builder()
                .id(4L)
                .userId(currentUserId)
                .tourId(10L)
                .viewedAt(LocalDateTime.of(2026, 4, 17, 9, 0))
                .build();
        UserTourView inactiveTour = UserTourView.builder()
                .id(3L)
                .userId(currentUserId)
                .tourId(11L)
                .viewedAt(LocalDateTime.of(2026, 4, 17, 8, 0))
                .build();

        Tour activeTour = activeTour(10L);
        Tour hiddenTour = activeTour(11L);
        hiddenTour.setStatus(TourStatus.INACTIVE);

        when(userTourViewRepository.findByUserIdOrderByViewedAtDesc(currentUserId))
                .thenReturn(List.of(latestTour10, olderTour10, inactiveTour));
        when(tourRepository.findAllById(anyIterable())).thenReturn(List.of(activeTour, hiddenTour));

        var responses = service.getMyTourViews();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getViewId()).isEqualTo(5L);
        assertThat(responses.get(0).getTourId()).isEqualTo(10L);
        assertThat(responses.get(0).getViewedAt()).isEqualTo(LocalDateTime.of(2026, 4, 17, 10, 0));
    }

    private void setAuthenticatedUser(UUID userId) {
        User user = User.builder()
                .id(userId)
                .email("viewer@example.com")
                .passwordHash("encoded-password")
                .fullName("Viewer")
                .build();
        CustomUserDetails principal = CustomUserDetails.fromUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private Tour activeTour(Long id) {
        return Tour.builder()
                .id(id)
                .code("TOUR-" + id)
                .name("Viewed Tour")
                .slug("viewed-tour-" + id)
                .basePrice(new BigDecimal("1300000"))
                .currency("VND")
                .durationDays(2)
                .durationNights(1)
                .shortDescription("Viewed tour summary")
                .isFeatured(false)
                .averageRating(new BigDecimal("4.20"))
                .totalReviews(9)
                .totalBookings(20)
                .status(TourStatus.ACTIVE)
                .build();
    }
}
