package com.wedservice.backend.module.bookings.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import com.wedservice.backend.module.bookings.entity.converter.BookingPaymentStatusConverter;
import com.wedservice.backend.module.bookings.entity.converter.BookingStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Convert(converter = BookingStatusConverter.class)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    @Convert(converter = BookingPaymentStatusConverter.class)
    @Column(name = "payment_status", length = 30, nullable = false)
    @Builder.Default
    private BookingPaymentStatus paymentStatus = BookingPaymentStatus.UNPAID;

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

    @Column(name = "discount_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "voucher_discount_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal voucherDiscountAmount = BigDecimal.ZERO;

    @Column(name = "loyalty_discount_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal loyaltyDiscountAmount = BigDecimal.ZERO;

    @Column(name = "addon_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal addonAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "combo_id")
    private Long comboId;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "VND";

}
