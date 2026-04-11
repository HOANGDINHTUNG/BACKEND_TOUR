package com.wedservice.backend.module.payments.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRefundRequest {
    @NotNull
    private BigDecimal approvedAmount;
}
