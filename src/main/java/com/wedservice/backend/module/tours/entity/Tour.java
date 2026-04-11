package com.wedservice.backend.module.tours.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import com.wedservice.backend.module.destinations.entity.Destination;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", unique = true, nullable = false, length = 280)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(name = "cancellation_policy_id")
    private Long cancellationPolicyId;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "highlights", columnDefinition = "TEXT")
    private String highlights;

    @Column(name = "inclusions", columnDefinition = "TEXT")
    private String inclusions;

    @Column(name = "exclusions", columnDefinition = "TEXT")
    private String exclusions;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "duration_nights", nullable = false)
    @Builder.Default
    private Integer durationNights = 0;

    @Column(name = "base_price", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "transport_type", length = 120)
    private String transportType;

    @Column(name = "trip_mode", length = 50)
    private String tripMode;

    @Column(name = "difficulty_level")
    @Builder.Default
    private Integer difficultyLevel = 1;

    @Column(name = "activity_level")
    @Builder.Default
    private Integer activityLevel = 1;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "min_group_size", nullable = false)
    @Builder.Default
    private Integer minGroupSize = 1;

    @Column(name = "max_group_size", nullable = false)
    @Builder.Default
    private Integer maxGroupSize = 50;

    @Column(name = "is_student_friendly", nullable = false)
    @Builder.Default
    private Boolean isStudentFriendly = false;

    @Column(name = "is_family_friendly", nullable = false)
    @Builder.Default
    private Boolean isFamilyFriendly = false;

    @Column(name = "is_senior_friendly", nullable = false)
    @Builder.Default
    private Boolean isSeniorFriendly = false;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "average_rating", precision = 3, scale = 2, nullable = false)
    @Builder.Default
    private java.math.BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_bookings", nullable = false)
    @Builder.Default
    private Integer totalBookings = 0;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "draft";

    @Column(name = "created_by", length = 36)
    private java.util.UUID createdBy;

    @Column(name = "updated_by", length = 36)
    private java.util.UUID updatedBy;
}
