package com.wedservice.backend.module.bookings.facade;

import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.service.command.BookingCommandService;
import com.wedservice.backend.module.bookings.service.query.BookingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingFacade {

    private final BookingCommandService bookingCommandService;
    private final BookingQueryService bookingQueryService;

    public BookingResponse createBooking(CreateBookingRequest request) {
        // Orchestration (validation, seat lock, payment trigger) can be added here
        return bookingCommandService.createBooking(request);
    }

    public BookingResponse getBooking(Long id) {
        return bookingQueryService.getBooking(id);
    }
}
