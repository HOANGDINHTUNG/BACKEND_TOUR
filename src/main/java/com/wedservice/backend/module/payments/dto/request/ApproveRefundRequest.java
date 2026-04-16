package com.wedservice.backend.module.payments.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRefundRequest {
    @NotNull
    @Positive
    private BigDecimal approvedAmount;
}
