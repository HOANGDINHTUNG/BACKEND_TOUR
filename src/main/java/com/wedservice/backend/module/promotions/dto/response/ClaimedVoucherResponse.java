package com.wedservice.backend.module.promotions.dto.response;

import com.wedservice.backend.module.promotions.entity.ClaimedVoucherStatus;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.users.entity.MemberLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ClaimedVoucherResponse {
    private Long claimId;
    private Long voucherId;
    private String voucherCode;
    private String voucherName;
    private String voucherDescription;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private VoucherApplicableScope applicableScope;
    private Long applicableTourId;
    private Long applicableDestinationId;
    private MemberLevel applicableMemberLevel;
    private Integer usageLimitTotal;
    private Integer usageLimitPerUser;
    private Integer totalUsedCount;
    private Integer userUsedCount;
    private Integer remainingUserUses;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean isStackable;
    private Boolean isActive;
    private LocalDateTime claimedAt;
    private ClaimedVoucherStatus status;
}
