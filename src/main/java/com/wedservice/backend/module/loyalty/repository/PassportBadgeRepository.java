package com.wedservice.backend.module.loyalty.repository;

import com.wedservice.backend.module.loyalty.entity.PassportBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassportBadgeRepository extends JpaRepository<PassportBadge, Long> {

    List<PassportBadge> findByPassportIdOrderByUnlockedAtDesc(Long passportId);

    Optional<PassportBadge> findByPassportIdAndBadgeId(Long passportId, Long badgeId);
}
