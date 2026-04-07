package com.wedservice.backend.module.destinations.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.UUID;

@Entity
@Table(name = "destinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true, nullable = false, updatable = false, length = 36)
    private UUID uuid;

    @Column(name = "code", unique = true, nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", unique = true, nullable = false, length = 220)
    private String slug;

    @Column(name = "country_code", nullable = false, length = 2)
    @Builder.Default
    private String countryCode = "VN";

    @Column(name = "province", nullable = false, length = 120)
    private String province;

    @Column(name = "district", length = 120)
    private String district;

    @Column(name = "region", length = 120)
    private String region;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "best_time_from_month")
    private Integer bestTimeFromMonth;

    @Column(name = "best_time_to_month")
    private Integer bestTimeToMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "crowd_level_default", nullable = false)
    @Builder.Default
    private CrowdLevel crowdLevelDefault = CrowdLevel.MEDIUM;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DestinationStatus status = DestinationStatus.APPROVED;

    @Column(name = "proposed_by", length = 36)
    private UUID proposedBy;

    @Column(name = "verified_by", length = 36)
    private UUID verifiedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_official", nullable = false)
    @Builder.Default
    private Boolean isOfficial = false;

    @PrePersist
    protected void beforeInsert() {
        super.onCreate();
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (status == null) {
            status = DestinationStatus.APPROVED;
        }
    }

    @PreUpdate
    protected void beforeUpdate() {
        super.onUpdate();
    }
}
