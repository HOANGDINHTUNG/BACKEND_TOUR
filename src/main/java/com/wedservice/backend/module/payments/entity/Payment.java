package com.wedservice.backend.module.payments.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import com.wedservice.backend.module.payments.entity.converter.PaymentStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_code", unique = true, length = 50)
    private String paymentCode;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "payment_method", length = 30, nullable = false)
    private String paymentMethod;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "transaction_ref", length = 150)
    private String transactionRef;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Convert(converter = PaymentStatusConverter.class)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "request_payload", columnDefinition = "JSON")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "JSON")
    private String responsePayload;
}
