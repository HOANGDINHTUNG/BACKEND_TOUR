package com.wedservice.backend.module.loyalty.service;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.loyalty.dto.request.CreateUserCheckinRequest;
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
import com.wedservice.backend.module.loyalty.service.MissionTrackerService;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPassportServiceTest {

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TravelPassportRepository travelPassportRepository;
    @Mock
    private PassportBadgeRepository passportBadgeRepository;
    @Mock
    private BadgeDefinitionRepository badgeDefinitionRepository;
    @Mock
    private PassportVisitedDestinationRepository passportVisitedDestinationRepository;
    @Mock
    private DestinationRepository destinationRepository;
    @Mock
    private UserCheckinRepository userCheckinRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private AuditTrailRecorder auditTrailRecorder;
    @Mock
    private MissionTrackerService missionTrackerService;

    private UserPassportService userPassportService;

    @BeforeEach
    void setUp() {
        userPassportService = new UserPassportService(
                authenticatedUserProvider,
                userRepository,
                travelPassportRepository,
                passportBadgeRepository,
                badgeDefinitionRepository,
                passportVisitedDestinationRepository,
                destinationRepository,
                userCheckinRepository,
                bookingRepository,
                tourRepository,
                auditTrailRecorder,
                missionTrackerService
        );
    }

    @Test
    void getMyPassport_bootstrapsMissingPassportAndReturnsNestedData() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("Traveler")
                .email("traveler@example.com")
                .passwordHash("encoded")
                .status(Status.ACTIVE)
                .build();

        TravelPassport createdPassport = TravelPassport.builder()
                .id(11L)
                .userId(userId)
                .passportNo("TVP1234")
                .totalTours(2)
                .totalDestinations(1)
                .totalCheckins(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PassportBadge passportBadge = PassportBadge.builder()
                .id(21L)
                .passportId(11L)
                .badgeId(31L)
                .unlockedAt(LocalDateTime.of(2026, 4, 17, 10, 0))
                .build();
        BadgeDefinition badgeDefinition = BadgeDefinition.builder()
                .id(31L)
                .code("FIRST_TRIP")
                .name("First Trip")
                .isActive(true)
                .build();
        PassportVisitedDestination visitedDestination = PassportVisitedDestination.builder()
                .id(41L)
                .passportId(11L)
                .destinationId(51L)
                .firstBookingId(61L)
                .firstVisitedAt(LocalDateTime.of(2026, 4, 1, 8, 0))
                .lastVisitedAt(LocalDateTime.of(2026, 4, 10, 8, 0))
                .build();
        Destination destination = Destination.builder()
                .id(51L)
                .uuid(UUID.randomUUID())
                .name("Da Nang")
                .slug("da-nang")
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(travelPassportRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(travelPassportRepository.save(any(TravelPassport.class))).thenReturn(createdPassport);
        when(passportBadgeRepository.findByPassportIdOrderByUnlockedAtDesc(11L)).thenReturn(List.of(passportBadge));
        when(badgeDefinitionRepository.findAllById(List.of(31L))).thenReturn(List.of(badgeDefinition));
        when(passportVisitedDestinationRepository.findByPassportIdOrderByLastVisitedAtDesc(11L))
                .thenReturn(List.of(visitedDestination));
        when(destinationRepository.findAllById(List.of(51L))).thenReturn(List.of(destination));

        var response = userPassportService.getMyPassport();

        assertThat(response.getPassportId()).isEqualTo(11L);
        assertThat(response.getBadges()).hasSize(1);
        assertThat(response.getBadges().get(0).getBadgeCode()).isEqualTo("FIRST_TRIP");
        assertThat(response.getVisitedDestinations()).hasSize(1);
        assertThat(response.getVisitedDestinations().get(0).getDestinationName()).isEqualTo("Da Nang");
        verify(travelPassportRepository).save(any(TravelPassport.class));
    }

    @Test
    void createCheckinForUser_createsCheckinAndSyncsPassportStats() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("Traveler")
                .email("traveler@example.com")
                .passwordHash("encoded")
                .status(Status.ACTIVE)
                .build();
        Booking booking = Booking.builder()
                .id(55L)
                .bookingCode("BK55")
                .userId(userId)
                .tourId(66L)
                .status(BookingStatus.CHECKED_IN)
                .paymentStatus(BookingPaymentStatus.PAID)
                .build();
        Destination destination = Destination.builder()
                .id(77L)
                .uuid(UUID.randomUUID())
                .name("Hoi An")
                .slug("hoi-an")
                .build();
        Tour tour = Tour.builder()
                .id(66L)
                .destination(destination)
                .build();
        TravelPassport passport = TravelPassport.builder()
                .id(88L)
                .userId(userId)
                .passportNo("TVP777")
                .totalTours(0)
                .totalDestinations(0)
                .totalCheckins(0)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(55L)).thenReturn(Optional.of(booking));
        when(tourRepository.findById(66L)).thenReturn(Optional.of(tour));
        when(userCheckinRepository.findFirstByBookingIdAndUserId(55L, userId)).thenReturn(Optional.empty());
        when(userCheckinRepository.save(any(UserCheckin.class))).thenAnswer(invocation -> {
            UserCheckin checkin = invocation.getArgument(0);
            checkin.setId(99L);
            checkin.setCreatedAt(LocalDateTime.of(2026, 4, 17, 12, 0));
            return checkin;
        });
        when(travelPassportRepository.findByUserId(userId)).thenReturn(Optional.of(passport));
        when(passportVisitedDestinationRepository.findByPassportIdAndDestinationId(88L, 77L)).thenReturn(Optional.empty());
        when(passportVisitedDestinationRepository.save(any(PassportVisitedDestination.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userCheckinRepository.countByUserId(userId)).thenReturn(1L);
        when(passportVisitedDestinationRepository.countByPassportId(88L)).thenReturn(1L);
        when(travelPassportRepository.save(any(TravelPassport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(destinationRepository.findById(77L)).thenReturn(Optional.of(destination));

        var response = userPassportService.createCheckinForUser(userId, CreateUserCheckinRequest.builder()
                .bookingId(55L)
                .note("Arrived")
                .build(), true);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getDestinationName()).isEqualTo("Hoi An");
        verify(auditTrailRecorder).record(any(), eq(99L), any(), any());
    }
}
