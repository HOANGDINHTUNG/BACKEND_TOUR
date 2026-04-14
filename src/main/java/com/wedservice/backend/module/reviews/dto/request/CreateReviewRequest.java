package com.wedservice.backend.module.reviews.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "Booking id is required")
    private Long bookingId;

    @Min(value = 1, message = "Overall rating must be between 1 and 5")
    @Max(value = 5, message = "Overall rating must be between 1 and 5")
    private Integer overallRating;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String content;

    private Boolean wouldRecommend;

    private List<@Valid CreateReviewAspectRequest> aspects;
}
