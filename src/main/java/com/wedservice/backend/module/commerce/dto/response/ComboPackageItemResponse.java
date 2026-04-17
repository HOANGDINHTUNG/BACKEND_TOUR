package com.wedservice.backend.module.commerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ComboPackageItemResponse {
    private Long id;
    private String itemType;
    private Long itemRefId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
