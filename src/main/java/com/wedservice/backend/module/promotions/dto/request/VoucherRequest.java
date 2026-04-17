package com.wedservice.backend.module.promotions.dto.request;

import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.users.entity.MemberLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRequest {

    @NotBlank(message = "code is required")
    @Size(max = 50, message = "code must not exceed 50 characters")
    private String code;

    private Long campaignId;

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "discountType is required")
    private VoucherDiscountType discountType;

    @NotNull(message = "discountValue is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "discountValue must be >= 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", inclusive = true, message = "maxDiscountAmount must be > 0")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "minOrderValue is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "minOrderValue must be >= 0")
    private BigDecimal minOrderValue;

    @Min(value = 1, message = "usageLimitTotal must be >= 1")
    private Integer usageLimitTotal;

    @Min(value = 1, message = "usageLimitPerUser must be >= 1")
    private Integer usageLimitPerUser;

    @NotNull(message = "applicableScope is required")
    private VoucherApplicableScope applicableScope;

    private Long applicableTourId;

    private Long applicableDestinationId;

    private MemberLevel applicableMemberLevel;

    @NotNull(message = "startAt is required")
    private LocalDateTime startAt;

    @NotNull(message = "endAt is required")
    private LocalDateTime endAt;

    private Boolean isStackable;

    private Boolean isActive;
}
