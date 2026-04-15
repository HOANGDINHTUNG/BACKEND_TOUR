package com.wedservice.backend.module.payments.service.command.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.entity.PaymentStatus;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private PaymentCommandServiceImpl paymentCommandService;

    @BeforeEach
    void setUp() {
        paymentCommandService = new PaymentCommandServiceImpl(
                paymentRepository,
                bookingRepository,
                authenticatedUserProvider
        );
    }

    @Test
    void createPayment_marksPaymentAndBookingAsPaid() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(15L)
                .userId(userId)
                .tourId(2L)
                .scheduleId(3L)
                .contactName("Nguyen Van B")
                .contactPhone("0909111111")
                .status(BookingStatus.PENDING_PAYMENT)
                .paymentStatus(BookingPaymentStatus.UNPAID)
                .build();

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .bookingId(15L)
                .paymentMethod("qr")
                .provider("vnpay")
                .transactionRef("TXN-001")
                .amount(new BigDecimal("1500000"))
                .build();

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(50L);
            return payment;
        });
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentCommandService.createPayment(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PAID);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getPaymentStatus()).isEqualTo(BookingPaymentStatus.PAID);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getStatus()).isEqualTo("paid");
    }
}
