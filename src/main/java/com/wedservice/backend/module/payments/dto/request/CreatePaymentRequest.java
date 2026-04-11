package com.wedservice.backend.module.payments.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    private String paymentMethod;

    private String provider;

    private String transactionRef;

    @NotNull
    private BigDecimal amount;
}
