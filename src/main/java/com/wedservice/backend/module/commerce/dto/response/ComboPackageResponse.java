package com.wedservice.backend.module.commerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ComboPackageResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private Boolean isActive;
    private List<ComboPackageItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
