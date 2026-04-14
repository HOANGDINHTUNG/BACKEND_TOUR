package com.wedservice.backend.module.reviews.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private UUID userId;
    private Long tourId;
    private Long scheduleId;
    private Integer overallRating;
    private String title;
    private String content;
    private String sentiment;
    private Boolean wouldRecommend;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewAspectResponse> aspects;
    private List<ReviewReplyResponse> replies;
}
