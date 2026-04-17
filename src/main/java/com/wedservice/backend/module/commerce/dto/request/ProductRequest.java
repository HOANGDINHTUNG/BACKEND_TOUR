package com.wedservice.backend.module.commerce.dto.request;

import com.wedservice.backend.module.commerce.entity.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "sku is required")
    @Size(max = 50, message = "sku must not exceed 50 characters")
    private String sku;

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "productType is required")
    private ProductType productType;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "unitPrice must be >= 0")
    private BigDecimal unitPrice;

    @NotNull(message = "stockQty is required")
    private Integer stockQty;

    private Boolean isGiftable;

    private Boolean isActive;
}
