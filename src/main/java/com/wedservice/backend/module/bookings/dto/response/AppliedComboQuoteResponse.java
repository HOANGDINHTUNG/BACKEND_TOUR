package com.wedservice.backend.module.bookings.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AppliedComboQuoteResponse {
    private Long comboId;
    private String comboCode;
    private String comboName;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
}
