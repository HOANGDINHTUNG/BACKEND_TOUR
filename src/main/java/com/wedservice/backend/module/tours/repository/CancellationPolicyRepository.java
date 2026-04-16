package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {

    Optional<CancellationPolicy> findByIdAndIsActiveTrue(Long id);

    Optional<CancellationPolicy> findFirstByIsDefaultTrueAndIsActiveTrue();
}
