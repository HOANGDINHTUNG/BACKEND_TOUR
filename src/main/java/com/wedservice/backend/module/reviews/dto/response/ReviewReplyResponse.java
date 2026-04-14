package com.wedservice.backend.module.reviews.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyResponse {
    private Long id;
    private UUID staffId;
    private String content;
    private LocalDateTime createdAt;
}
