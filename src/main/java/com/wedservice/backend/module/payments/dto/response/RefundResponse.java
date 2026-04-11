package com.wedservice.backend.module.payments.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private Long id;
    private String refundCode;
    private Long bookingId;
    private String status;
    private BigDecimal requestedAmount;
}
