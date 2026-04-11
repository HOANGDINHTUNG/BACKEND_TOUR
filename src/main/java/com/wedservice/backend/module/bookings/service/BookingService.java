package com.wedservice.backend.module.bookings.service;

import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBooking(Long id);

}
