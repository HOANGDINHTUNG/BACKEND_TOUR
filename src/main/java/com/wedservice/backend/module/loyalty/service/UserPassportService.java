package com.wedservice.backend.module.loyalty.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.loyalty.dto.request.CreateUserCheckinRequest;
import com.wedservice.backend.module.loyalty.dto.response.PassportBadgeResponse;
import com.wedservice.backend.module.loyalty.dto.response.PassportVisitedDestinationResponse;
import com.wedservice.backend.module.loyalty.dto.response.TravelPassportResponse;
import com.wedservice.backend.module.loyalty.dto.response.UserCheckinResponse;
import com.wedservice.backend.module.loyalty.entity.BadgeDefinition;
import com.wedservice.backend.module.loyalty.entity.PassportBadge;
import com.wedservice.backend.module.loyalty.entity.PassportVisitedDestination;
import com.wedservice.backend.module.loyalty.entity.TravelPassport;
import com.wedservice.backend.module.loyalty.entity.UserCheckin;
import com.wedservice.backend.module.loyalty.repository.BadgeDefinitionRepository;
import com.wedservice.backend.module.loyalty.repository.PassportBadgeRepository;
import com.wedservice.backend.module.loyalty.repository.PassportVisitedDestinationRepository;
import com.wedservice.backend.module.loyalty.repository.TravelPassportRepository;
import com.wedservice.backend.module.loyalty.repository.UserCheckinRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPassportService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserRepository userRepository;
    private final TravelPassportRepository travelPassportRepository;
    private final PassportBadgeRepository passportBadgeRepository;
    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final PassportVisitedDestinationRepository passportVisitedDestinationRepository;
    private final DestinationRepository destinationRepository;
    private final UserCheckinRepository userCheckinRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final AuditTrailRecorder auditTrailRecorder;
    private final MissionTrackerService missionTrackerService;

    @Transactional
    public TravelPassportResponse getMyPassport() {
        User user = findCurrentUser();
        TravelPassport passport = ensurePassport(user);

        List<PassportBadge> passportBadges = passportBadgeRepository.findByPassportIdOrderByUnlockedAtDesc(passport.getId());
        Map<Long, BadgeDefinition> badgeMap = badgeDefinitionRepository.findAllById(
                passportBadges.stream().map(PassportBadge::getBadgeId).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(BadgeDefinition::getId, item -> item));

        List<PassportVisitedDestination> visitedDestinations = passportVisitedDestinationRepository
                .findByPassportIdOrderByLastVisitedAtDesc(passport.getId());
        Map<Long, Destination> destinationMap = new LinkedHashMap<>();
        destinationRepository.findAllById(visitedDestinations.stream().map(PassportVisitedDestination::getDestinationId).toList())
                .forEach(destination -> destinationMap.put(destination.getId(), destination));

        return TravelPassportResponse.builder()
                .passportId(passport.getId())
                .userId(passport.getUserId())
                .passportNo(passport.getPassportNo())
                .totalTours(passport.getTotalTours())
                .totalDestinations(passport.getTotalDestinations())
                .totalCheckins(passport.getTotalCheckins())
                .lastTripAt(passport.getLastTripAt())
                .createdAt(passport.getCreatedAt())
                .updatedAt(passport.getUpdatedAt())
                .badges(passportBadges.stream()
                        .map(item -> toBadgeResponse(item, badgeMap.get(item.getBadgeId())))
                        .filter(java.util.Objects::nonNull)
                        .toList())
                .visitedDestinations(visitedDestinations.stream()
                        .map(item -> toVisitedDestinationResponse(item, destinationMap.get(item.getDestinationId())))
                        .filter(java.util.Objects::nonNull)
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserCheckinResponse> getMyCheckins() {
        User user = findCurrentUser();
        return mapCheckins(userCheckinRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @Transactional
    public UserCheckinResponse createCheckinForUser(UUID userId, CreateUserCheckinRequest request, boolean audit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("User is not active");
        }

        Destination resolvedDestination = resolveDestination(userId, request);
        Booking booking = resolveBooking(userId, request.getBookingId());

        if (booking != null && resolvedDestination != null) {
            Tour bookingTour = findTour(booking.getTourId());
            Long bookingDestinationId = bookingTour.getDestination() == null ? null : bookingTour.getDestination().getId();
            if (!resolvedDestination.getId().equals(bookingDestinationId)) {
                throw new BadRequestException("destinationUuid does not match booking destination");
            }
        }

        if (booking == null && resolvedDestination == null) {
            throw new BadRequestException("bookingId or destinationUuid is required");
        }

        Long destinationId = resolvedDestination != null
                ? resolvedDestination.getId()
                : findTour(booking.getTourId()).getDestination().getId();

        if (booking != null) {
            UserCheckin existing = userCheckinRepository.findFirstByBookingIdAndUserId(booking.getId(), userId).orElse(null);
            if (existing != null) {
                return mapCheckin(existing, booking, resolvedDestination != null ? resolvedDestination : findTour(booking.getTourId()).getDestination());
            }
        }

        UserCheckin checkin = userCheckinRepository.save(UserCheckin.builder()
                .userId(userId)
                .bookingId(booking == null ? null : booking.getId())
                .destinationId(destinationId)
                .checkinLatitude(request.getCheckinLatitude())
                .checkinLongitude(request.getCheckinLongitude())
                .note(normalizeNullable(request.getNote()))
                .build());

        syncPassportAfterCheckin(user, destinationId, booking == null ? null : booking.getId(), checkin.getCreatedAt());

        Destination destination = resolvedDestination != null
                ? resolvedDestination
                : destinationRepository.findById(destinationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));
        UserCheckinResponse response = mapCheckin(checkin, booking, destination);
        if (audit) {
            auditTrailRecorder.record(AuditActionType.USER_CHECKIN_CREATE, checkin.getId(), null, response);
        }
        return response;
    }

    @Transactional
    public void recordBookingCheckIn(Booking booking, String note) {
        if (booking == null || booking.getId() == null || booking.getUserId() == null) {
            return;
        }
        if (userCheckinRepository.findFirstByBookingIdAndUserId(booking.getId(), booking.getUserId()).isPresent()) {
            return;
        }
        createCheckinForUser(
                booking.getUserId(),
                CreateUserCheckinRequest.builder()
                        .bookingId(booking.getId())
                        .note(note)
                        .build(),
                false
        );
    }

    TravelPassport ensurePassport(User user) {
        return travelPassportRepository.findByUserId(user.getId())
                .orElseGet(() -> travelPassportRepository.save(TravelPassport.builder()
                        .userId(user.getId())
                        .passportNo(generatePassportNo(user.getId()))
                        .totalTours(0)
                        .totalDestinations(0)
                        .totalCheckins(0)
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }

    private void syncPassportAfterCheckin(User user, Long destinationId, Long bookingId, LocalDateTime checkinAt) {
        TravelPassport passport = ensurePassport(user);
        PassportVisitedDestination visitedDestination = passportVisitedDestinationRepository
                .findByPassportIdAndDestinationId(passport.getId(), destinationId)
                .orElseGet(() -> PassportVisitedDestination.builder()
                        .passportId(passport.getId())
                        .destinationId(destinationId)
                        .firstBookingId(bookingId)
                        .firstVisitedAt(checkinAt)
                        .build());
        if (visitedDestination.getFirstVisitedAt() == null) {
            visitedDestination.setFirstVisitedAt(checkinAt);
        }
        if (visitedDestination.getFirstBookingId() == null) {
            visitedDestination.setFirstBookingId(bookingId);
        }
        visitedDestination.setLastVisitedAt(checkinAt);
        passportVisitedDestinationRepository.save(visitedDestination);

        passport.setTotalCheckins((int) userCheckinRepository.countByUserId(user.getId()));
        passport.setTotalDestinations((int) passportVisitedDestinationRepository.countByPassportId(passport.getId()));
        travelPassportRepository.save(passport);
        missionTrackerService.incrementProgress(user.getId(), "TOTAL_CHECKINS", java.math.BigDecimal.ONE);
    }

    private User findCurrentUser() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getStatus() != Status.ACTIVE) {
            throw new UnauthorizedException("Your account is " + user.getStatus().getValue() + ". Please contact support.");
        }
        return user;
    }

    private String generatePassportNo(UUID userId) {
        String suffix = userId.toString().replace("-", "");
        return "TVP" + System.currentTimeMillis() + suffix.substring(Math.max(0, suffix.length() - 4));
    }

    private Booking resolveBooking(UUID userId, Long bookingId) {
        if (bookingId == null) {
            return null;
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        if (!userId.equals(booking.getUserId())) {
            throw new BadRequestException("bookingId does not belong to the user");
        }
        return booking;
    }

    private Destination resolveDestination(UUID userId, CreateUserCheckinRequest request) {
        if (!StringUtils.hasText(request.getDestinationUuid())) {
            return null;
        }
        try {
            UUID destinationUuid = UUID.fromString(request.getDestinationUuid());
            return destinationRepository.findByUuid(destinationUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + destinationUuid));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("destinationUuid must be a valid UUID");
        }
    }

    private Tour findTour(Long tourId) {
        return tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + tourId));
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private List<UserCheckinResponse> mapCheckins(List<UserCheckin> checkins) {
        if (checkins.isEmpty()) {
            return List.of();
        }
        Map<Long, Booking> bookingMap = new LinkedHashMap<>();
        List<Long> bookingIds = checkins.stream().map(UserCheckin::getBookingId).filter(java.util.Objects::nonNull).distinct().toList();
        if (!bookingIds.isEmpty()) {
            bookingRepository.findAllById(bookingIds).forEach(booking -> bookingMap.put(booking.getId(), booking));
        }
        Map<Long, Destination> destinationMap = new LinkedHashMap<>();
        List<Long> destinationIds = checkins.stream().map(UserCheckin::getDestinationId).filter(java.util.Objects::nonNull).distinct().toList();
        if (!destinationIds.isEmpty()) {
            destinationRepository.findAllById(destinationIds).forEach(destination -> destinationMap.put(destination.getId(), destination));
        }
        List<UserCheckinResponse> responses = new ArrayList<>();
        for (UserCheckin item : checkins) {
            responses.add(mapCheckin(item, bookingMap.get(item.getBookingId()), destinationMap.get(item.getDestinationId())));
        }
        return responses;
    }

    private UserCheckinResponse mapCheckin(UserCheckin checkin, Booking booking, Destination destination) {
        return UserCheckinResponse.builder()
                .id(checkin.getId())
                .userId(checkin.getUserId())
                .bookingId(checkin.getBookingId())
                .bookingCode(booking == null ? null : booking.getBookingCode())
                .destinationId(checkin.getDestinationId())
                .destinationUuid(destination == null ? null : destination.getUuid())
                .destinationName(destination == null ? null : destination.getName())
                .destinationSlug(destination == null ? null : destination.getSlug())
                .checkinLatitude(checkin.getCheckinLatitude())
                .checkinLongitude(checkin.getCheckinLongitude())
                .note(checkin.getNote())
                .createdAt(checkin.getCreatedAt())
                .build();
    }

    private PassportBadgeResponse toBadgeResponse(PassportBadge passportBadge, BadgeDefinition badge) {
        if (badge == null) {
            return null;
        }
        return PassportBadgeResponse.builder()
                .passportBadgeId(passportBadge.getId())
                .badgeId(badge.getId())
                .badgeCode(badge.getCode())
                .badgeName(badge.getName())
                .badgeDescription(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .conditionJson(badge.getConditionJson())
                .isActive(badge.getIsActive())
                .unlockedAt(passportBadge.getUnlockedAt())
                .build();
    }

    private PassportVisitedDestinationResponse toVisitedDestinationResponse(
            PassportVisitedDestination visitedDestination,
            Destination destination
    ) {
        if (destination == null) {
            return null;
        }
        return PassportVisitedDestinationResponse.builder()
                .visitedId(visitedDestination.getId())
                .destinationId(destination.getId())
                .destinationUuid(destination.getUuid())
                .destinationName(destination.getName())
                .destinationSlug(destination.getSlug())
                .firstBookingId(visitedDestination.getFirstBookingId())
                .firstVisitedAt(visitedDestination.getFirstVisitedAt())
                .lastVisitedAt(visitedDestination.getLastVisitedAt())
                .build();
    }
}
