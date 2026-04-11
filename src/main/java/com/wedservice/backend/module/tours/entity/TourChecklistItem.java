package com.wedservice.backend.module.tours.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_checklist_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourChecklistItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "item_name", length = 200, nullable = false)
    private String itemName;

    @Column(name = "item_group", length = 80)
    private String itemGroup;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;
}
