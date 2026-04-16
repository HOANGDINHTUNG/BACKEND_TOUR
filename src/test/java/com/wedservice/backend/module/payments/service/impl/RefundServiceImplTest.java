package com.wedservice.backend.module.payments.service.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.service.BookingStatusHistoryRecorder;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.entity.PaymentStatus;
import com.wedservice.backend.module.payments.entity.RefundRequest;
import com.wedservice.backend.module.payments.entity.RefundStatus;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.payments.repository.RefundRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    @Mock
    private RefundRequestRepository refundRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private BookingStatusHistoryRecorder bookingStatusHistoryRecorder;

    private RefundServiceImpl refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundServiceImpl(
                refundRepository,
                jdbcTemplate,
                paymentRepository,
                bookingRepository,
                authenticatedUserProvider,
                bookingStatusHistoryRecorder
        );
    }

    @Test
    void createRefundRequest_savesQuotedRefundForPaidBooking() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(15L)
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .finalAmount(new BigDecimal("1500000"))
                .build();

        CreateRefundRequest request = CreateRefundRequest.builder()
                .bookingId(15L)
                .reasonType("cancel_by_user")
                .reasonDetail("Need to reschedule")
                .requestedAmount(new BigDecimal("1200000"))
                .build();

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(refundRepository.existsByBookingIdAndStatusIn(15L, List.of(
                RefundStatus.REQUESTED,
                RefundStatus.QUOTED,
                RefundStatus.APPROVED,
                RefundStatus.PROCESSING,
                RefundStatus.COMPLETED
        ))).thenReturn(false);
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest refund = invocation.getArgument(0);
            refund.setId(90L);
            return refund;
        });

        try (MockedConstruction<SimpleJdbcCall> ignored = mockConstruction(SimpleJdbcCall.class, (mock, context) -> {
            when(mock.withProcedureName("sp_get_refund_quote")).thenReturn(mock);
            when(mock.execute(any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class))).thenReturn(Map.of(
                    "refundable_amount", new BigDecimal("1300000"),
                    "voucher_offer_amount", new BigDecimal("100000")
            ));
        })) {
            RefundResponse response = refundService.createRefundRequest(request);

            ArgumentCaptor<RefundRequest> refundCaptor = ArgumentCaptor.forClass(RefundRequest.class);
            verify(refundRepository).save(refundCaptor.capture());
            RefundRequest savedRefund = refundCaptor.getValue();

            assertThat(savedRefund.getRequestedAmount()).isEqualByComparingTo("1200000");
            assertThat(savedRefund.getQuotedAmount()).isEqualByComparingTo("1300000");
            assertThat(savedRefund.getVoucherOfferAmount()).isEqualByComparingTo("100000");
            assertThat(savedRefund.getStatus()).isEqualTo(RefundStatus.REQUESTED);
            assertThat(savedRefund.getRequestedBy()).isEqualTo(userId);

            assertThat(response.getId()).isEqualTo(90L);
            assertThat(response.getStatus()).isEqualTo("requested");
        }
    }

    @Test
    void createRefundRequest_rejectsBookingThatIsNotPaid() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(15L)
                .userId(userId)
                .status(BookingStatus.PENDING_PAYMENT)
                .paymentStatus(BookingPaymentStatus.UNPAID)
                .finalAmount(new BigDecimal("1500000"))
                .build();

        CreateRefundRequest request = CreateRefundRequest.builder()
                .bookingId(15L)
                .requestedAmount(new BigDecimal("1000000"))
                .build();

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);

        assertThatThrownBy(() -> refundService.createRefundRequest(request))
                .hasMessageContaining("Only paid bookings can create a refund request");

        verify(refundRepository, never()).save(any(RefundRequest.class));
    }

    @Test
    void approveRefund_marksBookingRefundedAndCreatesRefundPayment() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(15L)
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .finalAmount(new BigDecimal("1500000"))
                .build();
        RefundRequest refund = RefundRequest.builder()
                .id(90L)
                .refundCode("RF001")
                .bookingId(15L)
                .requestedAmount(new BigDecimal("1200000"))
                .quotedAmount(new BigDecimal("1300000"))
                .status(RefundStatus.REQUESTED)
                .build();

        when(refundRepository.findById(90L)).thenReturn(Optional.of(refund));
        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(120L);
            return payment;
        });
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundResponse response = refundService.approveRefund(90L, null, new BigDecimal("1100000"));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(paymentCaptor.getValue().getAmount()).isEqualByComparingTo("1100000");

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.REFUNDED);
        assertThat(bookingCaptor.getValue().getPaymentStatus()).isEqualTo(BookingPaymentStatus.REFUNDED);
        verify(bookingStatusHistoryRecorder).record(
                15L,
                BookingStatus.CONFIRMED,
                BookingStatus.REFUNDED,
                userId,
                "Refund approved"
        );

        ArgumentCaptor<RefundRequest> refundCaptor = ArgumentCaptor.forClass(RefundRequest.class);
        verify(refundRepository).save(refundCaptor.capture());
        assertThat(refundCaptor.getValue().getStatus()).isEqualTo(RefundStatus.APPROVED);
        assertThat(refundCaptor.getValue().getProcessedBy()).isEqualTo(userId);

        assertThat(response.getStatus()).isEqualTo("approved");
    }

    @Test
    void approveRefund_rejectsAmountAboveQuotedAmount() {
        UUID userId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(15L)
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .finalAmount(new BigDecimal("1500000"))
                .build();
        RefundRequest refund = RefundRequest.builder()
                .id(90L)
                .bookingId(15L)
                .requestedAmount(new BigDecimal("1200000"))
                .quotedAmount(new BigDecimal("1000000"))
                .status(RefundStatus.REQUESTED)
                .build();

        when(refundRepository.findById(90L)).thenReturn(Optional.of(refund));
        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);

        assertThatThrownBy(() -> refundService.approveRefund(90L, null, new BigDecimal("1100000")))
                .hasMessageContaining("Approved refund amount cannot exceed quoted refundable amount");

        verify(refundRepository, never()).save(any(RefundRequest.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, times(1)).findById(15L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
