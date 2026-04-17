package com.wedservice.backend.module.promotions.dto.response;

import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.users.entity.MemberLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private Long id;
    private String code;
    private Long campaignId;
    private String name;
    private String description;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer usageLimitTotal;
    private Integer usageLimitPerUser;
    private Integer usedCount;
    private VoucherApplicableScope applicableScope;
    private Long applicableTourId;
    private Long applicableDestinationId;
    private MemberLevel applicableMemberLevel;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean isStackable;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
