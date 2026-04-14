package com.wedservice.backend.module.reviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerateReviewRequest {
    @NotBlank(message = "Sentiment is required")
    private String sentiment;
}
