package com.wedservice.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Lớp cha dùng chung cho các entity cần theo dõi thời gian 


@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
