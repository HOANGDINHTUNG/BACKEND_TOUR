package com.wedservice.backend.module.tours.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_schedule_guides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourScheduleGuide extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "guide_id", nullable = false)
    private Long guideId;

    @Column(name = "guide_role", length = 80, nullable = false)
    @Builder.Default
    private String guideRole = "main";

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
}
