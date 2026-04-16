package com.wedservice.backend.module.payments.service.command.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.service.BookingStatusHistoryRecorder;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private static final Set<BookingStatus> PAYABLE_BOOKING_STATUSES = Set.of(
            BookingStatus.PENDING_PAYMENT,
            BookingStatus.CONFIRMED
    );

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final BookingStatusHistoryRecorder bookingStatusHistoryRecorder;

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        ensureCanAccessBooking(booking);
        validatePaymentRequest(request, booking);

        Payment p = Payment.builder()
                .paymentCode("PM" + System.currentTimeMillis())
                .bookingId(request.getBookingId())
                .paymentMethod(DataNormalizer.normalize(request.getPaymentMethod()))
                .provider(DataNormalizer.normalize(request.getProvider()))
                .transactionRef(DataNormalizer.normalize(request.getTransactionRef()))
                .amount(request.getAmount())
                .currency("VND")
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();

        p = paymentRepository.save(p);
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(BookingPaymentStatus.PAID);
        bookingRepository.save(booking);
        if (oldStatus != booking.getStatus()) {
            bookingStatusHistoryRecorder.record(
                    booking.getId(),
                    oldStatus,
                    booking.getStatus(),
                    authenticatedUserProvider.getRequiredCurrentUserId(),
                    "Payment recorded"
            );
        }

        return PaymentResponse.builder()
                .id(p.getId())
                .paymentCode(p.getPaymentCode())
                .bookingId(p.getBookingId())
                .amount(p.getAmount())
                .status(p.getStatus().getValue())
                .build();
    }

    private void validatePaymentRequest(CreatePaymentRequest request, Booking booking) {
        if (!PAYABLE_BOOKING_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Booking is not in a payable status");
        }
        if (booking.getPaymentStatus() == BookingPaymentStatus.PAID
                || booking.getPaymentStatus() == BookingPaymentStatus.REFUNDED) {
            throw new BadRequestException("Booking payment has already been completed");
        }
        if (paymentRepository.existsByBookingIdAndStatus(request.getBookingId(), PaymentStatus.PAID)) {
            throw new BadRequestException("A successful payment already exists for this booking");
        }
        if (booking.getFinalAmount() == null || booking.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Booking does not have a payable final amount");
        }
        if (request.getAmount().compareTo(booking.getFinalAmount()) != 0) {
            throw new BadRequestException("Payment amount must match booking final amount");
        }
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
