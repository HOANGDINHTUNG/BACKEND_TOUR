package com.wedservice.backend.module.promotions.entity;

import com.wedservice.backend.module.promotions.entity.converter.VoucherApplicableScopeConverter;
import com.wedservice.backend.module.promotions.entity.converter.VoucherDiscountTypeConverter;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.converter.MemberLevelConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Convert(converter = VoucherDiscountTypeConverter.class)
    @Column(name = "discount_type", nullable = false, length = 20)
    private VoucherDiscountType discountType;

    @Column(name = "discount_value", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 14, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_value", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "usage_limit_total")
    private Integer usageLimitTotal;

    @Column(name = "usage_limit_per_user", nullable = false)
    @Builder.Default
    private Integer usageLimitPerUser = 1;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Convert(converter = VoucherApplicableScopeConverter.class)
    @Column(name = "applicable_scope", nullable = false, length = 50)
    @Builder.Default
    private VoucherApplicableScope applicableScope = VoucherApplicableScope.ALL;

    @Column(name = "applicable_tour_id")
    private Long applicableTourId;

    @Column(name = "applicable_destination_id")
    private Long applicableDestinationId;

    @Convert(converter = MemberLevelConverter.class)
    @Column(name = "applicable_member_level", length = 20)
    private MemberLevel applicableMemberLevel;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_stackable", nullable = false)
    @Builder.Default
    private Boolean isStackable = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (usedCount == null) {
            usedCount = 0;
        }
        if (usageLimitPerUser == null) {
            usageLimitPerUser = 1;
        }
        if (applicableScope == null) {
            applicableScope = VoucherApplicableScope.ALL;
        }
        if (isStackable == null) {
            isStackable = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (usedCount == null) {
            usedCount = 0;
        }
        if (usageLimitPerUser == null) {
            usageLimitPerUser = 1;
        }
        if (applicableScope == null) {
            applicableScope = VoucherApplicableScope.ALL;
        }
        if (isStackable == null) {
            isStackable = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
