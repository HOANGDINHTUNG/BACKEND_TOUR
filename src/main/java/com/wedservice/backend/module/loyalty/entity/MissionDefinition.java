package com.wedservice.backend.module.loyalty.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.wedservice.backend.module.loyalty.entity.MissionRewardType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionDefinition extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "rule_json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String ruleJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private MissionRewardType rewardType;

    @Column(name = "reward_value", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal rewardValue = BigDecimal.ZERO;

    @Column(name = "reward_ref_id")
    private Long rewardRefId;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
