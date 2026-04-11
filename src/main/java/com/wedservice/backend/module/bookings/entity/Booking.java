package com.wedservice.backend.module.bookings.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", unique = true, length = 50)
    private String bookingCode;

    @Column(name = "user_id", nullable = false, length = 36)
    private java.util.UUID userId;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "pending_payment";

    @Column(name = "payment_status", length = 30, nullable = false)
    @Builder.Default
    private String paymentStatus = "unpaid";

    @Column(name = "contact_name", nullable = false, length = 150)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "adults", nullable = false)
    @Builder.Default
    private Integer adults = 1;

    @Column(name = "children", nullable = false)
    @Builder.Default
    private Integer children = 0;

    @Column(name = "infants", nullable = false)
    @Builder.Default
    private Integer infants = 0;

    @Column(name = "seniors", nullable = false)
    @Builder.Default
    private Integer seniors = 0;

    @Column(name = "subtotal_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
