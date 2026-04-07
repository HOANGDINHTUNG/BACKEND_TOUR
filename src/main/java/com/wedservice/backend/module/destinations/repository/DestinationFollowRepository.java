package com.wedservice.backend.module.destinations.repository;

import com.wedservice.backend.module.destinations.entity.DestinationFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DestinationFollowRepository extends JpaRepository<DestinationFollow, Long> {

    Optional<DestinationFollow> findByUserIdAndDestinationId(UUID userId, Long destinationId);

    boolean existsByUserIdAndDestinationId(UUID userId, Long destinationId);

    void deleteByUserIdAndDestinationId(UUID userId, Long destinationId);

    Page<DestinationFollow> findByUserId(UUID userId, Pageable pageable);
}
