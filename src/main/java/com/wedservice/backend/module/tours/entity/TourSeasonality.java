package com.wedservice.backend.module.tours.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tour_seasonality")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSeasonality extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "season_name", length = 100, nullable = false)
    private String seasonName;

    @Column(name = "month_from")
    private Integer monthFrom;

    @Column(name = "month_to")
    private Integer monthTo;

    @Column(name = "recommendation_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal recommendationScore = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
