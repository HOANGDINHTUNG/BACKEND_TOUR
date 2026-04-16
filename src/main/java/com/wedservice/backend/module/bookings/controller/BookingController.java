package com.wedservice.backend.module.bookings.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.UpdateBookingStatusRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.dto.response.BookingStatusHistoryResponse;
import com.wedservice.backend.module.bookings.facade.BookingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}/status-history")
    @PreAuthorize("hasAuthority('booking.view')")
    public ApiResponse<List<BookingStatusHistoryResponse>> getBookingStatusHistory(@PathVariable Long id) {
        return ApiResponse.success(bookingFacade.getBookingStatusHistory(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('booking.cancel')")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) UpdateBookingStatusRequest request
    ) {
        BookingResponse response = bookingFacade.cancelBooking(id, request == null ? null : request.getReason());
        return ApiResponse.success(response, "Booking status updated");
    }

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAuthority('booking.checkin')")
    public ApiResponse<BookingResponse> checkInBooking(
            @PathVariable Long id,
            @RequestBody(required = false) UpdateBookingStatusRequest request
    ) {
        BookingResponse response = bookingFacade.checkInBooking(id, request == null ? null : request.getReason());
        return ApiResponse.success(response, "Booking status updated");
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('booking.update')")
    public ApiResponse<BookingResponse> completeBooking(
            @PathVariable Long id,
            @RequestBody(required = false) UpdateBookingStatusRequest request
    ) {
        BookingResponse response = bookingFacade.completeBooking(id, request == null ? null : request.getReason());
        return ApiResponse.success(response, "Booking status updated");
    }
}
