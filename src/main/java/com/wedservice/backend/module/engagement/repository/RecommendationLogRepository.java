package com.wedservice.backend.module.engagement.repository;

import com.wedservice.backend.module.engagement.entity.RecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {

    List<RecommendationLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
