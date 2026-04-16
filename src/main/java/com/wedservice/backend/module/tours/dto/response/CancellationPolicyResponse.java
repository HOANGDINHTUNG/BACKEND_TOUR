package com.wedservice.backend.module.tours.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationPolicyResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal voucherBonusPercent;
    private Boolean isDefault;
    private Boolean isActive;
    private List<CancellationPolicyRuleResponse> rules;
}
