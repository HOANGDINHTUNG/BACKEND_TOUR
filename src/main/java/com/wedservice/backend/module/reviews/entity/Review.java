package com.wedservice.backend.module.reviews.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, length = 36)
    private UUID userId;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sentiment", nullable = false, length = 20)
    @Builder.Default
    private String sentiment = ReviewSentiment.NEUTRAL.getValue();

    @Column(name = "would_recommend", nullable = false)
    @Builder.Default
    private Boolean wouldRecommend = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    protected void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (sentiment == null || sentiment.isBlank()) {
            sentiment = ReviewSentiment.NEUTRAL.getValue();
        }
        if (wouldRecommend == null) {
            wouldRecommend = true;
        }
    }

    @jakarta.persistence.PreUpdate
    protected void beforeUpdate() {
        updatedAt = LocalDateTime.now();
        if (sentiment == null || sentiment.isBlank()) {
            sentiment = ReviewSentiment.NEUTRAL.getValue();
        }
        if (wouldRecommend == null) {
            wouldRecommend = true;
        }
    }
}
