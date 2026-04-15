package com.wedservice.backend.module.bookings.service.command.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.CreatePassengerRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPassenger;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingPassengerRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCommandServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingPassengerRepository passengerRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private BookingCommandServiceImpl bookingCommandService;

    @BeforeEach
    void setUp() {
        bookingCommandService = new BookingCommandServiceImpl(
                bookingRepository,
                passengerRepository,
                authenticatedUserProvider
        );
    }

    @Test
    void createBooking_usesCurrentUserAndInitialLifecycleStatuses() {
        UUID currentUserId = UUID.randomUUID();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(UUID.randomUUID().toString())
                .tourId(10L)
                .scheduleId(22L)
                .contactName("Nguyen Van A")
                .contactPhone("0909000000")
                .contactEmail("a@example.com")
                .adults(2)
                .children(1)
                .passengers(List.of(CreatePassengerRequest.builder()
                        .fullName("Passenger One")
                        .passengerType("adult")
                        .gender("male")
                        .phone("0909000000")
                        .build()))
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
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

        ArgumentCaptor<BookingPassenger> passengerCaptor = ArgumentCaptor.forClass(BookingPassenger.class);
        verify(passengerRepository).save(passengerCaptor.capture());
        assertThat(passengerCaptor.getValue().getBookingId()).isEqualTo(99L);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getStatus()).isEqualTo("pending_payment");
    }
}
