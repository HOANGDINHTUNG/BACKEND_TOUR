package com.wedservice.backend.module.payments.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import com.wedservice.backend.module.payments.entity.converter.RefundStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refund_code", unique = true, length = 50)
    private String refundCode;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "requested_by", length = 36)
    private java.util.UUID requestedBy;

    @Column(name = "reason_type", length = 100)
    private String reasonType;

    @Column(name = "reason_detail", columnDefinition = "TEXT")
    private String reasonDetail;

    @Column(name = "requested_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal requestedAmount = BigDecimal.ZERO;

    @Column(name = "quoted_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal quotedAmount = BigDecimal.ZERO;

    @Column(name = "approved_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal approvedAmount = BigDecimal.ZERO;

    @Column(name = "voucher_offer_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal voucherOfferAmount = BigDecimal.ZERO;

    @Column(name = "refund_method", length = 30)
    private String refundMethod;

    @Convert(converter = RefundStatusConverter.class)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private RefundStatus status = RefundStatus.REQUESTED;

    @Column(name = "policy_snapshot", columnDefinition = "JSON")
    private String policySnapshot;

    @Column(name = "processed_by", length = 36)
    private java.util.UUID processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
