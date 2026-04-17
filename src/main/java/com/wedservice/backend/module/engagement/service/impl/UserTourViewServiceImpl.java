package com.wedservice.backend.module.engagement.service.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.engagement.dto.response.ViewedTourResponse;
import com.wedservice.backend.module.engagement.entity.UserTourView;
import com.wedservice.backend.module.engagement.repository.UserTourViewRepository;
import com.wedservice.backend.module.engagement.service.command.UserTourViewCommandService;
import com.wedservice.backend.module.engagement.service.query.UserTourViewQueryService;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserTourViewServiceImpl implements UserTourViewCommandService, UserTourViewQueryService {

    private static final long VIEW_COOLDOWN_MINUTES = 30;

    private final UserTourViewRepository userTourViewRepository;
    private final TourRepository tourRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<ViewedTourResponse> getMyTourViews() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        List<UserTourView> rows = userTourViewRepository.findByUserIdOrderByViewedAtDesc(userId);
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<Long, UserTourView> latestPerTour = new LinkedHashMap<>();
        for (UserTourView row : rows) {
            latestPerTour.putIfAbsent(row.getTourId(), row);
        }

        Map<Long, Tour> tourMap = new LinkedHashMap<>();
        tourRepository.findAllById(latestPerTour.keySet()).stream()
                .filter(this::isVisibleTour)
                .forEach(tour -> tourMap.put(tour.getId(), tour));

        return latestPerTour.values().stream()
                .filter(row -> tourMap.containsKey(row.getTourId()))
                .map(row -> toResponse(row, tourMap.get(row.getTourId())))
                .toList();
    }

    @Override
    @Transactional
    public void recordCurrentUserTourViewIfAuthenticated(Long tourId) {
        UUID userId = resolveCurrentUserIdIfAuthenticated();
        if (userId == null) {
            return;
        }

        Tour tour = tourRepository.findById(tourId)
                .filter(this::isVisibleTour)
                .orElse(null);
        if (tour == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(VIEW_COOLDOWN_MINUTES);
        UserTourView latestView = userTourViewRepository.findTopByUserIdAndTourIdOrderByViewedAtDesc(userId, tourId)
                .orElse(null);
        if (latestView != null && latestView.getViewedAt() != null && !latestView.getViewedAt().isBefore(threshold)) {
            return;
        }

        userTourViewRepository.save(UserTourView.builder()
                .userId(userId)
                .tourId(tourId)
                .viewedAt(now)
                .build());
    }

    private UUID resolveCurrentUserIdIfAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getUserId();
        }

        return null;
    }

    private boolean isVisibleTour(Tour tour) {
        return tour != null
                && tour.getDeletedAt() == null
                && tour.getStatus() == TourStatus.ACTIVE;
    }

    private ViewedTourResponse toResponse(UserTourView row, Tour tour) {
        return ViewedTourResponse.builder()
                .viewId(row.getId())
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
                .viewedAt(row.getViewedAt())
                .build();
    }
}
