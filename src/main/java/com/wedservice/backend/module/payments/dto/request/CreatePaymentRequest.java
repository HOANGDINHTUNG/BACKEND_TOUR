package com.wedservice.backend.module.payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank
    private String paymentMethod;

    private String provider;

    private String transactionRef;

    @NotNull
    @Positive
    private BigDecimal amount;
}
