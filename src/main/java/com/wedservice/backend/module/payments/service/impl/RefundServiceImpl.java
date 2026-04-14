package com.wedservice.backend.module.payments.service.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.entity.RefundRequest;
import com.wedservice.backend.module.payments.repository.RefundRequestRepository;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.service.command.RefundCommandService;
import com.wedservice.backend.module.payments.service.query.RefundQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundCommandService, RefundQueryService {

    private final RefundRequestRepository refundRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public RefundResponse createRefundRequest(CreateRefundRequest request) {
    Booking booking = findBooking(request.getBookingId());
    ensureCanAccessBooking(booking, "refund");

    // Call stored procedure sp_get_refund_quote to get quote and policy snapshot
    SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withProcedureName("sp_get_refund_quote");
    MapSqlParameterSource in = new MapSqlParameterSource().addValue("p_booking_id", request.getBookingId());
    Map<String, Object> out = Optional.ofNullable(call.execute(in)).orElse(Map.of());

    // The stored procedure returns columns like refundable_amount and voucher_offer_amount
    Object refundableObj = out.getOrDefault("refundable_amount", null);
    Object voucherObj = out.getOrDefault("voucher_offer_amount", null);
    Object policySnapshot = out; // store full map as JSON-like string

    RefundRequest r = RefundRequest.builder()
        .refundCode("RF" + System.currentTimeMillis())
        .bookingId(request.getBookingId())
        .reasonType(request.getReasonType())
        .reasonDetail(request.getReasonDetail())
        .requestedAmount(request.getRequestedAmount())
        .quotedAmount(refundableObj instanceof Number ? new java.math.BigDecimal(((Number) refundableObj).doubleValue()) : java.math.BigDecimal.ZERO)
        .voucherOfferAmount(voucherObj instanceof Number ? new java.math.BigDecimal(((Number) voucherObj).doubleValue()) : java.math.BigDecimal.ZERO)
        .policySnapshot(policySnapshot.toString())
        .requestedBy(resolveRequestedBy(request.getRequestedBy()))
        .status("requested")
        .build();

    r = refundRepository.save(r);

    return RefundResponse.builder()
        .id(r.getId())
        .refundCode(r.getRefundCode())
        .bookingId(r.getBookingId())
        .status(r.getStatus())
        .requestedAmount(r.getRequestedAmount())
        .build();
    }

    @Override
    public RefundResponse getRefund(Long id) {
        RefundRequest r = refundRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Refund not found"));
        Booking booking = findBooking(r.getBookingId());
        ensureCanAccessBooking(booking, "refund");

        return RefundResponse.builder()
                .id(r.getId())
                .refundCode(r.getRefundCode())
                .bookingId(r.getBookingId())
                .status(r.getStatus())
                .requestedAmount(r.getRequestedAmount())
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public RefundResponse approveRefund(Long id, String processedBy, java.math.BigDecimal approvedAmount) {
        RefundRequest r = refundRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Refund not found"));
        Booking booking = findBooking(r.getBookingId());
        ensureCanAccessBooking(booking, "refund");

        r.setApprovedAmount(approvedAmount);
        if (StringUtils.hasText(processedBy)) {
            try {
                r.setProcessedBy(java.util.UUID.fromString(processedBy));
            } catch (Exception ignored) {
            }
        } else {
            r.setProcessedBy(authenticatedUserProvider.getRequiredCurrentUserId());
        }
        r.setProcessedAt(java.time.LocalDateTime.now());
        r.setStatus("approved");

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
                .status("refunded")
                .paidAt(java.time.LocalDateTime.now())
                .build();

        paymentRepository.save(p);

        // Update booking payment status to indicate refund
        try {
            Booking bookingToUpdate = bookingRepository.findById(r.getBookingId()).orElse(null);
            if (bookingToUpdate != null) {
                bookingToUpdate.setPaymentStatus("refunded");
                bookingRepository.save(bookingToUpdate);
            }
        } catch (Exception ignored) {
        }

        return RefundResponse.builder()
                .id(r.getId())
                .refundCode(r.getRefundCode())
                .bookingId(r.getBookingId())
                .status(r.getStatus())
                .requestedAmount(r.getRequestedAmount())
                .build();
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    private void ensureCanAccessBooking(Booking booking, String resourceName) {
        if (authenticatedUserProvider.isCurrentUserBackoffice()) {
            return;
        }
        if (!authenticatedUserProvider.getRequiredCurrentUserId().equals(booking.getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this " + resourceName);
        }
    }

    private java.util.UUID resolveRequestedBy(String requestedBy) {
        if (authenticatedUserProvider.isCurrentUserBackoffice() && StringUtils.hasText(requestedBy)) {
            try {
                return java.util.UUID.fromString(requestedBy);
            } catch (Exception ignored) {
            }
        }
        return authenticatedUserProvider.getRequiredCurrentUserId();
    }
}
