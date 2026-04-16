package com.wedservice.backend.module.bookings.service.query;

import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.dto.response.BookingStatusHistoryResponse;

import java.util.List;

public interface BookingQueryService {
    BookingResponse getBooking(Long id);
    List<BookingStatusHistoryResponse> getBookingStatusHistory(Long id);
}
