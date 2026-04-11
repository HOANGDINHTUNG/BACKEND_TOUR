package com.wedservice.backend.module.bookings.service.query;

import com.wedservice.backend.module.bookings.dto.response.BookingResponse;

public interface BookingQueryService {
    BookingResponse getBooking(Long id);
}
