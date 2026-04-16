package com.wedservice.backend.module.tours.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_policy_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "min_hours_before")
    private Integer minHoursBefore;

    @Column(name = "max_hours_before")
    private Integer maxHoursBefore;

    @Column(name = "refund_percent", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal refundPercent = BigDecimal.ZERO;

    @Column(name = "voucher_percent", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal voucherPercent = BigDecimal.ZERO;

    @Column(name = "fee_percent", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal feePercent = BigDecimal.ZERO;

    @Column(name = "allow_reschedule", nullable = false)
    @Builder.Default
    private Boolean allowReschedule = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
