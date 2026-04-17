package com.wedservice.backend.module.commerce.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboPackageRequest {

    @NotBlank(message = "code is required")
    @Size(max = 40, message = "code must not exceed 40 characters")
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "basePrice is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "basePrice must be >= 0")
    private BigDecimal basePrice;

    @NotNull(message = "discountAmount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "discountAmount must be >= 0")
    private BigDecimal discountAmount;

    private Boolean isActive;

    @Valid
    @NotEmpty(message = "items must not be empty")
    private List<ComboPackageItemRequest> items;
}
