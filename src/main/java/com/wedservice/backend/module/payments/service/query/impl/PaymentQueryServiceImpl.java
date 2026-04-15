package com.wedservice.backend.module.payments.service.query.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.payments.service.query.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public PaymentResponse getPayment(Long id) {
        Payment p = paymentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        Booking booking = bookingRepository.findById(p.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        ensureCanAccessBooking(booking);

        return PaymentResponse.builder()
                .id(p.getId())
                .paymentCode(p.getPaymentCode())
                .bookingId(p.getBookingId())
                .amount(p.getAmount())
                .status(p.getStatus().getValue())
                .build();
    }

    private void ensureCanAccessBooking(Booking booking) {
        if (authenticatedUserProvider.isCurrentUserBackoffice()) {
            return;
        }
        if (!authenticatedUserProvider.getRequiredCurrentUserId().equals(booking.getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this payment");
        }
    }
}
