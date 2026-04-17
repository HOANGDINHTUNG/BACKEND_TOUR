package com.wedservice.backend.module.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_estimates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_label", length = 255)
    private String fromLabel;

    @Column(name = "to_label", length = 255)
    private String toLabel;

    @Column(name = "from_latitude", precision = 10, scale = 7)
    private BigDecimal fromLatitude;

    @Column(name = "from_longitude", precision = 10, scale = 7)
    private BigDecimal fromLongitude;

    @Column(name = "to_latitude", precision = 10, scale = 7)
    private BigDecimal toLatitude;

    @Column(name = "to_longitude", precision = 10, scale = 7)
    private BigDecimal toLongitude;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "google_map_url", columnDefinition = "TEXT")
    private String googleMapUrl;

    @Column(name = "source_name", length = 100)
    private String sourceName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
