package com.wedservice.backend.module.payments.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRefundRequest {
    @NotNull
    private Long bookingId;

    private String requestedBy;

    private String reasonType;

    private String reasonDetail;

    @NotNull
    private BigDecimal requestedAmount;
}
