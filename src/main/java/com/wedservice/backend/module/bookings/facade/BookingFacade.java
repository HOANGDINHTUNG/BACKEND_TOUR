package com.wedservice.backend.module.bookings.facade;

import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.BookingQuoteRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingQuoteResponse;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.dto.response.BookingStatusHistoryResponse;
import com.wedservice.backend.module.bookings.service.command.BookingCommandService;
import com.wedservice.backend.module.bookings.service.BookingPricingService;
import com.wedservice.backend.module.bookings.service.query.BookingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingFacade {

    private final BookingCommandService bookingCommandService;
    private final BookingQueryService bookingQueryService;
    private final BookingPricingService bookingPricingService;

    public BookingQuoteResponse quoteBooking(BookingQuoteRequest request) {
        return bookingPricingService.quoteBooking(request);
    }

    public BookingResponse createBooking(CreateBookingRequest request) {
        // Orchestration (validation, seat lock, payment trigger) can be added here
        return bookingCommandService.createBooking(request);
    }

    public BookingResponse getBooking(Long id) {
        return bookingQueryService.getBooking(id);
    }

    public BookingResponse cancelBooking(Long id, String reason) {
        return bookingCommandService.cancelBooking(id, reason);
    }

    public BookingResponse checkInBooking(Long id, String reason) {
        return bookingCommandService.checkInBooking(id, reason);
    }

    public BookingResponse completeBooking(Long id, String reason) {
        return bookingCommandService.completeBooking(id, reason);
    }

    public List<BookingStatusHistoryResponse> getBookingStatusHistory(Long id) {
        return bookingQueryService.getBookingStatusHistory(id);
    }
}
