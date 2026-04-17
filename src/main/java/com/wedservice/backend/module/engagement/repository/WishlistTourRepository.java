package com.wedservice.backend.module.engagement.repository;

import com.wedservice.backend.module.engagement.entity.WishlistTour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistTourRepository extends JpaRepository<WishlistTour, Long> {

    List<WishlistTour> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<WishlistTour> findByUserIdAndTourId(UUID userId, Long tourId);

    boolean existsByUserIdAndTourId(UUID userId, Long tourId);
}
