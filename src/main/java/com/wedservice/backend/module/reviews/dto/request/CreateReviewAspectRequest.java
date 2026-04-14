package com.wedservice.backend.module.reviews.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewAspectRequest {

    @NotBlank(message = "Aspect name is required")
    @Size(max = 100, message = "Aspect name must not exceed 100 characters")
    private String aspectName;

    @Min(value = 1, message = "Aspect rating must be between 1 and 5")
    @Max(value = 5, message = "Aspect rating must be between 1 and 5")
    private Integer aspectRating;

    private String comment;
}
