package com.wedservice.backend.module.bookings.service.command;

import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;

public interface BookingCommandService {
    BookingResponse createBooking(CreateBookingRequest request);
    BookingResponse cancelBooking(Long id, String reason);
    BookingResponse checkInBooking(Long id, String reason);
    BookingResponse completeBooking(Long id, String reason);
}
