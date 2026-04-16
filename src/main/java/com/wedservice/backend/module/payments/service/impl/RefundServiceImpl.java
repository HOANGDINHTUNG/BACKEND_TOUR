package com.wedservice.backend.module.payments.service.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.service.BookingStatusHistoryRecorder;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.entity.RefundRequest;
import com.wedservice.backend.module.payments.entity.RefundStatus;
import com.wedservice.backend.module.payments.repository.RefundRequestRepository;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.entity.PaymentStatus;
import com.wedservice.backend.module.payments.service.command.RefundCommandService;
import com.wedservice.backend.module.payments.service.query.RefundQueryService;
import com.wedservice.backend.module.tours.service.TourRuntimeStatsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundCommandService, RefundQueryService {

    private static final List<RefundStatus> ACTIVE_REFUND_STATUSES = List.of(
            RefundStatus.REQUESTED,
            RefundStatus.QUOTED,
            RefundStatus.APPROVED,
            RefundStatus.PROCESSING,
            RefundStatus.COMPLETED
    );

    private final RefundRequestRepository refundRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final BookingStatusHistoryRecorder bookingStatusHistoryRecorder;
    private final TourRuntimeStatsSyncService tourRuntimeStatsSyncService;

    @Override
    @Transactional
    public RefundResponse createRefundRequest(CreateRefundRequest request) {
        Booking booking = findBooking(request.getBookingId());
        ensureCanAccessBooking(booking, "refund");
        validateCreateRefundRequest(request, booking);

        Map<String, Object> refundQuote = loadRefundQuote(request.getBookingId());
        BigDecimal quotedAmount = toBigDecimal(refundQuote.get("refundable_amount"));
        BigDecimal voucherOfferAmount = toBigDecimal(refundQuote.get("voucher_offer_amount"));
        validateRefundQuote(quotedAmount, request.getRequestedAmount());

        RefundRequest refund = RefundRequest.builder()
                .refundCode("RF" + System.currentTimeMillis())
                .bookingId(request.getBookingId())
                .reasonType(DataNormalizer.normalize(request.getReasonType()))
                .reasonDetail(DataNormalizer.normalize(request.getReasonDetail()))
                .requestedAmount(request.getRequestedAmount())
                .quotedAmount(quotedAmount)
                .voucherOfferAmount(voucherOfferAmount)
                .policySnapshot(refundQuote.toString())
                .requestedBy(resolveRequestedBy(request.getRequestedBy()))
                .status(RefundStatus.REQUESTED)
                .build();

        refund = refundRepository.save(refund);

        return RefundResponse.builder()
                .id(refund.getId())
                .refundCode(refund.getRefundCode())
                .bookingId(refund.getBookingId())
                .status(refund.getStatus().getValue())
                .requestedAmount(refund.getRequestedAmount())
                .build();
    }

    @Override
    public RefundResponse getRefund(Long id) {
        RefundRequest r = refundRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Refund not found"));
        Booking booking = findBooking(r.getBookingId());
        ensureCanAccessBooking(booking, "refund");

        return RefundResponse.builder()
                .id(r.getId())
                .refundCode(r.getRefundCode())
                .bookingId(r.getBookingId())
                .status(r.getStatus().getValue())
                .requestedAmount(r.getRequestedAmount())
                .build();
    }

    @Override
    @Transactional
    public RefundResponse approveRefund(Long id, String processedBy, BigDecimal approvedAmount) {
        RefundRequest r = refundRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Refund not found"));
        Booking booking = findBooking(r.getBookingId());
        ensureCanAccessBooking(booking, "refund");
        validateApproveRefund(r, booking, approvedAmount);

        r.setApprovedAmount(approvedAmount);
        r.setProcessedBy(resolveProcessedBy(processedBy));
        r.setProcessedAt(LocalDateTime.now());
        r.setStatus(RefundStatus.APPROVED);

        r = refundRepository.save(r);

        // create payment record to represent refund transaction
        Payment p = Payment.builder()
                .paymentCode("RFN" + System.currentTimeMillis())
                .bookingId(r.getBookingId())
                .paymentMethod("refund")
                .provider("system")
                .transactionRef(r.getRefundCode())
                .amount(approvedAmount)
                .currency("VND")
                .status(PaymentStatus.REFUNDED)
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(p);

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.REFUNDED);
        booking.setPaymentStatus(BookingPaymentStatus.REFUNDED);
        bookingRepository.save(booking);
        tourRuntimeStatsSyncService.syncScheduleState(booking.getScheduleId());
        tourRuntimeStatsSyncService.syncTourBookingStats(booking.getTourId());
        bookingStatusHistoryRecorder.record(
                booking.getId(),
                oldStatus,
                booking.getStatus(),
                authenticatedUserProvider.getRequiredCurrentUserId(),
                "Refund approved"
        );

        return RefundResponse.builder()
                .id(r.getId())
                .refundCode(r.getRefundCode())
                .bookingId(r.getBookingId())
                .status(r.getStatus().getValue())
                .requestedAmount(r.getRequestedAmount())
                .build();
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void ensureCanAccessBooking(Booking booking, String resourceName) {
        if (authenticatedUserProvider.isCurrentUserBackoffice()) {
            return;
        }
        if (!authenticatedUserProvider.getRequiredCurrentUserId().equals(booking.getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this " + resourceName);
        }
    }

    private Map<String, Object> loadRefundQuote(Long bookingId) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withProcedureName("sp_get_refund_quote");
        MapSqlParameterSource in = new MapSqlParameterSource().addValue("p_booking_id", bookingId);
        return Optional.ofNullable(call.execute(in)).orElse(Map.of());
    }

    private void validateCreateRefundRequest(CreateRefundRequest request, Booking booking) {
        if (booking.getPaymentStatus() != BookingPaymentStatus.PAID) {
            throw new BadRequestException("Only paid bookings can create a refund request");
        }
        if (booking.getStatus() == BookingStatus.REFUNDED || booking.getPaymentStatus() == BookingPaymentStatus.REFUNDED) {
            throw new BadRequestException("Booking has already been refunded");
        }
        if (refundRepository.existsByBookingIdAndStatusIn(request.getBookingId(), ACTIVE_REFUND_STATUSES)) {
            throw new BadRequestException("An active refund request already exists for this booking");
        }
        if (booking.getFinalAmount() == null || booking.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Booking does not have a refundable amount");
        }
        if (request.getRequestedAmount().compareTo(booking.getFinalAmount()) > 0) {
            throw new BadRequestException("Requested refund amount cannot exceed booking final amount");
        }
    }

    private void validateRefundQuote(BigDecimal quotedAmount, BigDecimal requestedAmount) {
        if (quotedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Booking is not eligible for refund");
        }
        if (requestedAmount.compareTo(quotedAmount) > 0) {
            throw new BadRequestException("Requested refund amount cannot exceed quoted refundable amount");
        }
    }

    private void validateApproveRefund(RefundRequest refund, Booking booking, BigDecimal approvedAmount) {
        if (refund.getStatus() != RefundStatus.REQUESTED) {
            throw new BadRequestException("Refund request is not awaiting approval");
        }
        if (booking.getPaymentStatus() != BookingPaymentStatus.PAID) {
            throw new BadRequestException("Only paid bookings can be refunded");
        }
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Approved refund amount must be greater than 0");
        }
        if (approvedAmount.compareTo(refund.getRequestedAmount()) > 0) {
            throw new BadRequestException("Approved refund amount cannot exceed requested amount");
        }
        if (approvedAmount.compareTo(refund.getQuotedAmount()) > 0) {
            throw new BadRequestException("Approved refund amount cannot exceed quoted refundable amount");
        }
    }

    private UUID resolveRequestedBy(String requestedBy) {
        if (authenticatedUserProvider.isCurrentUserBackoffice() && StringUtils.hasText(requestedBy)) {
            try {
                return UUID.fromString(requestedBy);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("requestedBy must be a valid UUID");
            }
        }
        return authenticatedUserProvider.getRequiredCurrentUserId();
    }

    private UUID resolveProcessedBy(String processedBy) {
        if (!StringUtils.hasText(processedBy)) {
            return authenticatedUserProvider.getRequiredCurrentUserId();
        }
        try {
            return UUID.fromString(processedBy);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("processedBy must be a valid UUID");
        }
    }

    private BigDecimal toBigDecimal(Object rawValue) {
        if (rawValue == null) {
            return BigDecimal.ZERO;
        }
        if (rawValue instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (rawValue instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(rawValue.toString());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}
