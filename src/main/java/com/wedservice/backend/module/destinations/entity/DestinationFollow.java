package com.wedservice.backend.module.destinations.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "destination_follows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_destination_follow", columnNames = {"user_id", "destination_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 36)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(name = "notify_event", nullable = false)
    @Builder.Default
    private Boolean notifyEvent = true;

    @Column(name = "notify_voucher", nullable = false)
    @Builder.Default
    private Boolean notifyVoucher = true;

    @Column(name = "notify_new_tour", nullable = false)
    @Builder.Default
    private Boolean notifyNewTour = true;

    @Column(name = "notify_best_season", nullable = false)
    @Builder.Default
    private Boolean notifyBestSeason = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void beforeInsert() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
