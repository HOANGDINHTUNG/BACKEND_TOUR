package com.wedservice.backend.module.payments.service.command.impl;

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
import com.wedservice.backend.module.tours.service.TourRuntimeStatsSyncService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Mock
    private BookingStatusHistoryRecorder bookingStatusHistoryRecorder;

    @Mock
    private TourRuntimeStatsSyncService tourRuntimeStatsSyncService;

    private PaymentCommandServiceImpl paymentCommandService;

    @BeforeEach
    void setUp() {
        paymentCommandService = new PaymentCommandServiceImpl(
                paymentRepository,
                bookingRepository,
                authenticatedUserProvider,
                bookingStatusHistoryRecorder,
                tourRuntimeStatsSyncService
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
                .finalAmount(new BigDecimal("1500000"))
                .build();

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .bookingId(15L)
                .paymentMethod("qr")
                .provider("vnpay")
                .transactionRef("TXN-001")
                .amount(new BigDecimal("1500000"))
                .build();

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(15L, PaymentStatus.PAID)).thenReturn(false);
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
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(bookingCaptor.getValue().getPaymentStatus()).isEqualTo(BookingPaymentStatus.PAID);
        verify(bookingStatusHistoryRecorder).record(
                15L,
                BookingStatus.PENDING_PAYMENT,
                BookingStatus.CONFIRMED,
                userId,
                "Payment recorded"
        );
        verify(tourRuntimeStatsSyncService).syncScheduleState(3L);
        verify(tourRuntimeStatsSyncService).syncTourBookingStats(2L);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getStatus()).isEqualTo("paid");
    }

    @Test
    void createPayment_rejectsAmountMismatch() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(15L)
                .userId(userId)
                .status(BookingStatus.PENDING_PAYMENT)
                .paymentStatus(BookingPaymentStatus.UNPAID)
                .finalAmount(new BigDecimal("1500000"))
                .build();

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .bookingId(15L)
                .paymentMethod("qr")
                .amount(new BigDecimal("1499999"))
                .build();

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(15L, PaymentStatus.PAID)).thenReturn(false);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);

        assertThatThrownBy(() -> paymentCommandService.createPayment(request))
                .hasMessageContaining("Payment amount must match booking final amount");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createPayment_rejectsAlreadyPaidBooking() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(15L)
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .finalAmount(new BigDecimal("1500000"))
                .build();

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .bookingId(15L)
                .paymentMethod("qr")
                .amount(new BigDecimal("1500000"))
                .build();

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);

        assertThatThrownBy(() -> paymentCommandService.createPayment(request))
                .hasMessageContaining("Booking payment has already been completed");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
