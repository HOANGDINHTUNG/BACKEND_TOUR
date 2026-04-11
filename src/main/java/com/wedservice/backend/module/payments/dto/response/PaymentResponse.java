package com.wedservice.backend.module.payments.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private String paymentCode;
    private Long bookingId;
    private BigDecimal amount;
    private String status;
}
