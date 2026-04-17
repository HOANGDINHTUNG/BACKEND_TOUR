package com.wedservice.backend.module.promotions.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimVoucherRequest {

    @NotBlank(message = "voucherCode is required")
    @Size(max = 50, message = "voucherCode must not exceed 50 characters")
    private String voucherCode;
}
