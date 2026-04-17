package com.wedservice.backend.module.loyalty.repository;

import com.wedservice.backend.module.loyalty.entity.UserCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCheckinRepository extends JpaRepository<UserCheckin, Long> {

    List<UserCheckin> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<UserCheckin> findFirstByBookingIdAndUserId(Long bookingId, UUID userId);

    long countByUserId(UUID userId);
}
