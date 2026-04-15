package com.wedservice.backend.module.bookings.service.query.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.service.query.BookingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public BookingResponse getBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        ensureCanAccessBooking(booking);

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus().getValue())
                .finalAmount(booking.getFinalAmount())
                .build();
    }

    private void ensureCanAccessBooking(Booking booking) {
        if (authenticatedUserProvider.isCurrentUserBackoffice()) {
            return;
        }
        if (!authenticatedUserProvider.getRequiredCurrentUserId().equals(booking.getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this booking");
        }
    }
}
