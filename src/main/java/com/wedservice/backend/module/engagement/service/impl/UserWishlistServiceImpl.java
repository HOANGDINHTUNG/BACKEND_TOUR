package com.wedservice.backend.module.engagement.service.impl;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.engagement.dto.response.WishlistTourResponse;
import com.wedservice.backend.module.engagement.entity.WishlistTour;
import com.wedservice.backend.module.engagement.repository.WishlistTourRepository;
import com.wedservice.backend.module.engagement.service.command.UserWishlistCommandService;
import com.wedservice.backend.module.engagement.service.query.UserWishlistQueryService;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserWishlistServiceImpl implements UserWishlistCommandService, UserWishlistQueryService {

    private final WishlistTourRepository wishlistTourRepository;
    private final TourRepository tourRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<WishlistTourResponse> getMyWishlistTours() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        List<WishlistTour> wishlistTours = wishlistTourRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (wishlistTours.isEmpty()) {
            return List.of();
        }

        Map<Long, Tour> tourMap = new LinkedHashMap<>();
        tourRepository.findAllById(wishlistTours.stream().map(WishlistTour::getTourId).toList()).stream()
                .filter(this::isWishlistVisible)
                .forEach(tour -> tourMap.put(tour.getId(), tour));

        return wishlistTours.stream()
                .filter(item -> tourMap.containsKey(item.getTourId()))
                .map(item -> toResponse(item, tourMap.get(item.getTourId())))
                .toList();
    }

    @Override
    @Transactional
    public WishlistTourResponse addMyWishlistTour(Long tourId) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        Tour tour = findWishlistEligibleTour(tourId);

        WishlistTour wishlistTour = wishlistTourRepository.findByUserIdAndTourId(userId, tourId)
                .orElseGet(() -> wishlistTourRepository.save(WishlistTour.builder()
                        .userId(userId)
                        .tourId(tourId)
                        .build()));

        return toResponse(wishlistTour, tour);
    }

    @Override
    @Transactional
    public void removeMyWishlistTour(Long tourId) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        wishlistTourRepository.findByUserIdAndTourId(userId, tourId)
                .ifPresent(wishlistTourRepository::delete);
    }

    private Tour findWishlistEligibleTour(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + tourId));

        if (!isWishlistVisible(tour)) {
            throw new ResourceNotFoundException("Tour not found with id: " + tourId);
        }

        return tour;
    }

    private boolean isWishlistVisible(Tour tour) {
        return tour != null
                && tour.getDeletedAt() == null
                && tour.getStatus() == TourStatus.ACTIVE;
    }

    private WishlistTourResponse toResponse(WishlistTour wishlistTour, Tour tour) {
        return WishlistTourResponse.builder()
                .wishlistId(wishlistTour.getId())
                .tourId(tour.getId())
                .tourCode(tour.getCode())
                .tourName(tour.getName())
                .tourSlug(tour.getSlug())
                .destinationId(tour.getDestination() != null ? tour.getDestination().getId() : null)
                .basePrice(tour.getBasePrice())
                .currency(tour.getCurrency())
                .durationDays(tour.getDurationDays())
                .durationNights(tour.getDurationNights())
                .shortDescription(tour.getShortDescription())
                .isFeatured(tour.getIsFeatured())
                .averageRating(tour.getAverageRating())
                .totalReviews(tour.getTotalReviews())
                .totalBookings(tour.getTotalBookings())
                .wishedAt(wishlistTour.getCreatedAt())
                .build();
    }
}
