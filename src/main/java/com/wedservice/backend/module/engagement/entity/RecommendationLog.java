package com.wedservice.backend.module.engagement.entity;

import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.entity.converter.BudgetLevelConverter;
import com.wedservice.backend.module.users.entity.converter.PreferredTripModeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "recommendation_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "requested_tag", length = 100)
    private String requestedTag;

    @Convert(converter = BudgetLevelConverter.class)
    @Column(name = "requested_budget", length = 20)
    private BudgetLevel requestedBudget;

    @Convert(converter = PreferredTripModeConverter.class)
    @Column(name = "requested_trip_mode", length = 20)
    private PreferredTripMode requestedTripMode;

    @Column(name = "requested_people_count")
    private Integer requestedPeopleCount;

    @Column(name = "requested_departure_at")
    private LocalDateTime requestedDepartureAt;

    @Column(name = "generated_result", columnDefinition = "json")
    private String generatedResult;

    @Column(name = "scoring_detail", columnDefinition = "json")
    private String scoringDetail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @jakarta.persistence.PrePersist
    protected void beforeInsert() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
