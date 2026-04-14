package com.wedservice.backend.module.bookings.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.facade.BookingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingFacade bookingFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('booking.create')")
    public ApiResponse<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
    BookingResponse response = bookingFacade.createBooking(request);
        return ApiResponse.success(response, "Booking created");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('booking.view')")
    public ApiResponse<BookingResponse> getBooking(@PathVariable Long id) {
    BookingResponse response = bookingFacade.getBooking(id);
        return ApiResponse.success(response);
    }
}
