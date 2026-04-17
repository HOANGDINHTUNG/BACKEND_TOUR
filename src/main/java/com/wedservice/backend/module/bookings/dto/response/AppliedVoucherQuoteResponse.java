package com.wedservice.backend.module.bookings.dto.response;

import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AppliedVoucherQuoteResponse {
    private Long claimId;
    private Long voucherId;
    private String voucherCode;
    private String voucherName;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
}
