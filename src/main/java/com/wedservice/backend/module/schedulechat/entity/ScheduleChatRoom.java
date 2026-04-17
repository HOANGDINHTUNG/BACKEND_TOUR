package com.wedservice.backend.module.schedulechat.entity;

import com.wedservice.backend.module.schedulechat.entity.converter.ScheduleChatVisibilityConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false, unique = true)
    private Long scheduleId;

    @Column(name = "room_name", nullable = false, length = 200)
    private String roomName;

    @Convert(converter = ScheduleChatVisibilityConverter.class)
    @Column(name = "visibility", nullable = false, length = 20)
    @Builder.Default
    private ScheduleChatVisibility visibility = ScheduleChatVisibility.ALL_MEMBERS;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (visibility == null) {
            visibility = ScheduleChatVisibility.ALL_MEMBERS;
        }
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
    }
}
