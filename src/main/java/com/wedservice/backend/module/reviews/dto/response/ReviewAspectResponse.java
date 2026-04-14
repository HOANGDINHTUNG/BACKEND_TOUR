package com.wedservice.backend.module.reviews.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAspectResponse {
    private Long id;
    private String aspectName;
    private Integer aspectRating;
    private String comment;
}
