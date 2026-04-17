package com.wedservice.backend.module.commerce.dto.response;

import com.wedservice.backend.module.commerce.entity.ProductType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private ProductType productType;
    private BigDecimal unitPrice;
    private Integer stockQty;
    private Boolean isGiftable;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
