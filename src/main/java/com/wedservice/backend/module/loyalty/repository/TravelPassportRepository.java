package com.wedservice.backend.module.loyalty.repository;

import com.wedservice.backend.module.loyalty.entity.TravelPassport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TravelPassportRepository extends JpaRepository<TravelPassport, Long> {

    Optional<TravelPassport> findByUserId(UUID userId);

    boolean existsByPassportNo(String passportNo);
}
