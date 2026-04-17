package com.wedservice.backend.module.commerce.dto.request;

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
public class ComboPackageItemRequest {

    @NotBlank(message = "itemType is required")
    @Size(max = 50, message = "itemType must not exceed 50 characters")
    private String itemType;

    private Long itemRefId;

    @NotBlank(message = "itemName is required")
    @Size(max = 200, message = "itemName must not exceed 200 characters")
    private String itemName;

    @NotNull(message = "quantity is required")
    private Integer quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "unitPrice must be >= 0")
    private BigDecimal unitPrice;
}
