package com.wedservice.backend.module.loyalty.repository;

import com.wedservice.backend.module.loyalty.entity.PassportVisitedDestination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassportVisitedDestinationRepository extends JpaRepository<PassportVisitedDestination, Long> {

    List<PassportVisitedDestination> findByPassportIdOrderByLastVisitedAtDesc(Long passportId);

    java.util.Optional<PassportVisitedDestination> findByPassportIdAndDestinationId(Long passportId, Long destinationId);

    long countByPassportId(Long passportId);
}
