package com.wedservice.backend.module.engagement.repository;

import com.wedservice.backend.module.engagement.entity.UserTourView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTourViewRepository extends JpaRepository<UserTourView, Long> {

    Optional<UserTourView> findTopByUserIdAndTourIdOrderByViewedAtDesc(UUID userId, Long tourId);

    List<UserTourView> findByUserIdOrderByViewedAtDesc(UUID userId);
}
