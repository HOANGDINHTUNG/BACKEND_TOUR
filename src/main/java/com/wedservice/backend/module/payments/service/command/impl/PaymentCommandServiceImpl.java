package com.wedservice.backend.module.payments.service.command.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.entity.PaymentStatus;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.payments.service.command.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        ensureCanAccessBooking(booking);

        Payment p = Payment.builder()
                .paymentCode("PM" + System.currentTimeMillis())
                .bookingId(request.getBookingId())
                .paymentMethod(request.getPaymentMethod())
                .provider(request.getProvider())
                .transactionRef(request.getTransactionRef())
                .amount(request.getAmount())
                .currency("VND")
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();

        p = paymentRepository.save(p);
        booking.setPaymentStatus(BookingPaymentStatus.PAID);
        bookingRepository.save(booking);

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
