package com.wedservice.backend.module.bookings.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BookingQuoteResponse {
    private Long tourId;
    private Long scheduleId;
    private Integer adults;
    private Integer children;
    private Integer infants;
    private Integer seniors;
    private Integer seatCount;
    private Integer travellerCount;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal voucherDiscountAmount;
    private BigDecimal loyaltyDiscountAmount;
    private BigDecimal addonAmount;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private String currency;
    private AppliedVoucherQuoteResponse appliedVoucher;
    private AppliedComboQuoteResponse appliedCombo;
}
