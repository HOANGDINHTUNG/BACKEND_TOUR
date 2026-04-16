package com.wedservice.backend.module.bookings.service.command.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.CreatePassengerRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.service.BookingStatusHistoryRecorder;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPassenger;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingPassengerRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.validator.BookingValidator;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCommandServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingPassengerRepository passengerRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private BookingStatusHistoryRecorder bookingStatusHistoryRecorder;

    private BookingCommandServiceImpl bookingCommandService;

    @BeforeEach
    void setUp() {
        bookingCommandService = new BookingCommandServiceImpl(
                bookingRepository,
                passengerRepository,
                tourScheduleRepository,
                authenticatedUserProvider,
                new BookingValidator(),
                bookingStatusHistoryRecorder
        );
    }

    @Test
    void createBooking_usesCurrentUserCalculatesAmountsAndMapsPassengers() {
        UUID currentUserId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TourSchedule schedule = TourSchedule.builder()
                .id(22L)
                .tourId(10L)
                .departureAt(now.plusDays(10))
                .returnAt(now.plusDays(12))
                .bookingOpenAt(now.minusDays(5))
                .bookingCloseAt(now.plusDays(5))
                .capacityTotal(30)
                .bookedSeats(4)
                .adultPrice(new BigDecimal("100.00"))
                .childPrice(new BigDecimal("60.00"))
                .infantPrice(new BigDecimal("10.00"))
                .seniorPrice(new BigDecimal("80.00"))
                .status(TourScheduleStatus.OPEN)
                .build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(UUID.randomUUID().toString())
                .tourId(10L)
                .scheduleId(22L)
                .contactName("Nguyen Van A")
                .contactPhone("0909000000")
                .contactEmail("a@example.com")
                .adults(2)
                .children(1)
                .infants(1)
                .seniors(1)
                .passengers(List.of(
                        CreatePassengerRequest.builder()
                                .fullName("Passenger One")
                                .passengerType("adult")
                                .gender("male")
                                .dateOfBirth("1990-01-15")
                                .phone("0909 000 000")
                                .email("PASSENGER1@EXAMPLE.COM")
                                .build(),
                        CreatePassengerRequest.builder()
                                .fullName("Passenger Two")
                                .passengerType("child")
                                .gender("female")
                                .dateOfBirth("2015-06-01")
                                .build()
                ))
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(tourScheduleRepository.findById(22L)).thenReturn(Optional.of(schedule));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(99L);
            return booking;
        });
        when(passengerRepository.save(any(BookingPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingCommandService.createBooking(request);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();

        assertThat(savedBooking.getUserId()).isEqualTo(currentUserId);
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(savedBooking.getPaymentStatus()).isEqualTo(BookingPaymentStatus.UNPAID);
        assertThat(savedBooking.getSubtotalAmount()).isEqualByComparingTo("350.00");
        assertThat(savedBooking.getFinalAmount()).isEqualByComparingTo("350.00");

        ArgumentCaptor<BookingPassenger> passengerCaptor = ArgumentCaptor.forClass(BookingPassenger.class);
        verify(passengerRepository, times(2)).save(passengerCaptor.capture());
        List<BookingPassenger> savedPassengers = passengerCaptor.getAllValues();
        assertThat(savedPassengers).hasSize(2);
        assertThat(savedPassengers.get(0).getBookingId()).isEqualTo(99L);
        assertThat(savedPassengers.get(0).getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(savedPassengers.get(0).getPhone()).isEqualTo("0909000000");
        assertThat(savedPassengers.get(0).getEmail()).isEqualTo("passenger1@example.com");
        assertThat(savedPassengers.get(1).getPassengerType()).isEqualTo("child");

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getStatus()).isEqualTo("pending_payment");
        assertThat(response.getFinalAmount()).isEqualByComparingTo("350.00");
        verify(bookingStatusHistoryRecorder).record(
                99L,
                null,
                BookingStatus.PENDING_PAYMENT,
                currentUserId,
                "Booking created"
        );
    }

    @Test
    void createBooking_rejectsScheduleThatDoesNotBelongToTour() {
        UUID currentUserId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TourSchedule schedule = TourSchedule.builder()
                .id(22L)
                .tourId(999L)
                .departureAt(now.plusDays(10))
                .returnAt(now.plusDays(12))
                .bookingOpenAt(now.minusDays(2))
                .bookingCloseAt(now.plusDays(2))
                .capacityTotal(20)
                .bookedSeats(0)
                .adultPrice(new BigDecimal("100.00"))
                .status(TourScheduleStatus.OPEN)
                .build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(currentUserId.toString())
                .tourId(10L)
                .scheduleId(22L)
                .contactName("Nguyen Van A")
                .contactPhone("0909000000")
                .adults(1)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(tourScheduleRepository.findById(22L)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> bookingCommandService.createBooking(request))
                .hasMessageContaining("Schedule does not belong to the requested tour");
    }

    @Test
    void cancelBooking_movesPaidConfirmedBookingToCancelRequested() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(44L)
                .bookingCode("BK44")
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .finalAmount(new BigDecimal("1000"))
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(bookingRepository.findById(44L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingCommandService.cancelBooking(44L, "Customer requested cancellation");

        assertThat(response.getStatus()).isEqualTo("cancel_requested");
        verify(bookingStatusHistoryRecorder).record(
                44L,
                BookingStatus.CONFIRMED,
                BookingStatus.CANCEL_REQUESTED,
                userId,
                "Customer requested cancellation"
        );
    }

    @Test
    void checkInBooking_requiresConfirmedAndPaidBooking() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(45L)
                .userId(userId)
                .status(BookingStatus.PENDING_PAYMENT)
                .paymentStatus(BookingPaymentStatus.UNPAID)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(bookingRepository.findById(45L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingCommandService.checkInBooking(45L, "Arrival at gate"))
                .hasMessageContaining("Only confirmed bookings can be checked in");
    }
}
