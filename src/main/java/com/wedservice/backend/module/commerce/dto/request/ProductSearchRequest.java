package com.wedservice.backend.module.commerce.dto.request;

import com.wedservice.backend.module.commerce.entity.ProductType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductSearchRequest {

    @Min(value = 0, message = "page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private int size = 10;

    @Size(max = 100, message = "keyword must not exceed 100 characters")
    private String keyword;

    private ProductType productType;

    private Boolean isGiftable;

    private Boolean isActive;

    @Pattern(
            regexp = "sku|name|productType|unitPrice|stockQty|isGiftable|isActive|createdAt|updatedAt",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "sortBy is invalid"
    )
    private String sortBy = "createdAt";

    @Pattern(
            regexp = "asc|desc",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "sortDir must be asc or desc"
    )
    private String sortDir = "desc";
}
