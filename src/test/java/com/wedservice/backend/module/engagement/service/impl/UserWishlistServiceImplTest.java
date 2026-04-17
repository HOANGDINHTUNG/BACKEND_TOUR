package com.wedservice.backend.module.engagement.service.impl;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.engagement.entity.WishlistTour;
import com.wedservice.backend.module.engagement.repository.WishlistTourRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserWishlistServiceImplTest {

    @Mock
    private WishlistTourRepository wishlistTourRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UserWishlistServiceImpl service;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        service = new UserWishlistServiceImpl(wishlistTourRepository, tourRepository, authenticatedUserProvider);
        currentUserId = UUID.randomUUID();
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
    }

    @Test
    void addMyWishlistTour_createsNewWishlistEntryForActiveTour() {
        Tour tour = activeTour(11L);
        when(tourRepository.findById(11L)).thenReturn(Optional.of(tour));
        when(wishlistTourRepository.findByUserIdAndTourId(currentUserId, 11L)).thenReturn(Optional.empty());
        when(wishlistTourRepository.save(any(WishlistTour.class))).thenAnswer(invocation -> {
            WishlistTour entity = invocation.getArgument(0);
            entity.setId(99L);
            entity.setCreatedAt(LocalDateTime.of(2026, 4, 17, 1, 0));
            return entity;
        });

        var response = service.addMyWishlistTour(11L);

        ArgumentCaptor<WishlistTour> captor = ArgumentCaptor.forClass(WishlistTour.class);
        verify(wishlistTourRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(currentUserId);
        assertThat(captor.getValue().getTourId()).isEqualTo(11L);

        assertThat(response.getWishlistId()).isEqualTo(99L);
        assertThat(response.getTourId()).isEqualTo(11L);
        assertThat(response.getTourName()).isEqualTo("Wishlist Tour");
        assertThat(response.getBasePrice()).isEqualByComparingTo("1500000");
    }

    @Test
    void addMyWishlistTour_returnsExistingWishlistEntryWhenAlreadyPresent() {
        Tour tour = activeTour(11L);
        WishlistTour wishlistTour = WishlistTour.builder()
                .id(22L)
                .userId(currentUserId)
                .tourId(11L)
                .createdAt(LocalDateTime.of(2026, 4, 17, 1, 5))
                .build();

        when(tourRepository.findById(11L)).thenReturn(Optional.of(tour));
        when(wishlistTourRepository.findByUserIdAndTourId(currentUserId, 11L)).thenReturn(Optional.of(wishlistTour));

        var response = service.addMyWishlistTour(11L);

        verify(wishlistTourRepository, never()).save(any(WishlistTour.class));
        assertThat(response.getWishlistId()).isEqualTo(22L);
        assertThat(response.getWishedAt()).isEqualTo(LocalDateTime.of(2026, 4, 17, 1, 5));
    }

    @Test
    void addMyWishlistTour_rejectsInactiveTour() {
        Tour tour = activeTour(11L);
        tour.setStatus(TourStatus.INACTIVE);
        when(tourRepository.findById(11L)).thenReturn(Optional.of(tour));

        assertThatThrownBy(() -> service.addMyWishlistTour(11L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tour not found with id: 11");
    }

    @Test
    void getMyWishlistTours_returnsOnlyVisibleActiveToursInWishlistOrder() {
        WishlistTour newest = WishlistTour.builder()
                .id(2L)
                .userId(currentUserId)
                .tourId(22L)
                .createdAt(LocalDateTime.of(2026, 4, 17, 2, 0))
                .build();
        WishlistTour older = WishlistTour.builder()
                .id(1L)
                .userId(currentUserId)
                .tourId(11L)
                .createdAt(LocalDateTime.of(2026, 4, 17, 1, 0))
                .build();

        Tour hiddenTour = activeTour(22L);
        hiddenTour.setStatus(TourStatus.DRAFT);

        when(wishlistTourRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)).thenReturn(List.of(newest, older));
        when(tourRepository.findAllById(List.of(22L, 11L))).thenReturn(List.of(hiddenTour, activeTour(11L)));

        var responses = service.getMyWishlistTours();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTourId()).isEqualTo(11L);
        assertThat(responses.get(0).getWishlistId()).isEqualTo(1L);
    }

    @Test
    void removeMyWishlistTour_deletesExistingEntryAndIgnoresMissingEntry() {
        WishlistTour wishlistTour = WishlistTour.builder()
                .id(7L)
                .userId(currentUserId)
                .tourId(11L)
                .build();
        when(wishlistTourRepository.findByUserIdAndTourId(currentUserId, 11L)).thenReturn(Optional.of(wishlistTour));

        service.removeMyWishlistTour(11L);
        verify(wishlistTourRepository).delete(wishlistTour);

        when(wishlistTourRepository.findByUserIdAndTourId(currentUserId, 12L)).thenReturn(Optional.empty());
        service.removeMyWishlistTour(12L);
        verify(wishlistTourRepository, times(1)).delete(any(WishlistTour.class));
    }

    private Tour activeTour(Long id) {
        return Tour.builder()
                .id(id)
                .code("TOUR-" + id)
                .name("Wishlist Tour")
                .slug("wishlist-tour-" + id)
                .basePrice(new BigDecimal("1500000"))
                .currency("VND")
                .durationDays(3)
                .durationNights(2)
                .shortDescription("Short description")
                .isFeatured(true)
                .averageRating(new BigDecimal("4.70"))
                .totalReviews(18)
                .totalBookings(44)
                .status(TourStatus.ACTIVE)
                .build();
    }
}
