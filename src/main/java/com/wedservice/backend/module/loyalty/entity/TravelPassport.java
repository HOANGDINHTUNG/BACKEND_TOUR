package com.wedservice.backend.module.loyalty.entity;

import jakarta.persistence.Column;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "travel_passports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPassport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private UUID userId;

    @Column(name = "passport_no", unique = true, length = 50)
    private String passportNo;

    @Column(name = "total_tours", nullable = false)
    @Builder.Default
    private Integer totalTours = 0;

    @Column(name = "total_destinations", nullable = false)
    @Builder.Default
    private Integer totalDestinations = 0;

    @Column(name = "total_checkins", nullable = false)
    @Builder.Default
    private Integer totalCheckins = 0;

    @Column(name = "last_trip_at")
    private LocalDateTime lastTripAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (totalTours == null) {
            totalTours = 0;
        }
        if (totalDestinations == null) {
            totalDestinations = 0;
        }
        if (totalCheckins == null) {
            totalCheckins = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (totalTours == null) {
            totalTours = 0;
        }
        if (totalDestinations == null) {
            totalDestinations = 0;
        }
        if (totalCheckins == null) {
            totalCheckins = 0;
        }
    }
}
