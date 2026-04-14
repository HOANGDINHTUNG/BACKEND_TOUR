package com.wedservice.backend.module.reviews.repository;

import com.wedservice.backend.module.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBookingId(Long bookingId);
    Optional<Review> findByBookingId(Long bookingId);
    Page<Review> findByTourId(Long tourId, Pageable pageable);
    Page<Review> findByUserId(UUID userId, Pageable pageable);
}
