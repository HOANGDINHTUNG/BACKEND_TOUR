package com.wedservice.backend.module.reviews.repository;

import com.wedservice.backend.module.reviews.entity.ReviewAspect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewAspectRepository extends JpaRepository<ReviewAspect, Long> {
    List<ReviewAspect> findByReviewId(Long reviewId);
    void deleteByReviewId(Long reviewId);
}
