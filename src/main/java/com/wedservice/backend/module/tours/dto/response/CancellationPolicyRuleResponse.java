package com.wedservice.backend.module.tours.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationPolicyRuleResponse {

    private Long id;
    private Integer minHoursBefore;
    private Integer maxHoursBefore;
    private BigDecimal refundPercent;
    private BigDecimal voucherPercent;
    private BigDecimal feePercent;
    private Boolean allowReschedule;
    private String notes;
}
