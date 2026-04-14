package com.wedservice.backend.module.reviews.repository;

import com.wedservice.backend.module.reviews.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(Long reviewId);
}
