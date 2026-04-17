package com.wedservice.backend.module.loyalty.repository;

import com.wedservice.backend.module.loyalty.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findByUserId(UUID userId);
    Optional<UserMission> findByUserIdAndMissionId(UUID userId, Long missionId);
    Optional<UserMission> findByUserIdAndMissionCode(UUID userId, String missionCode);
}
