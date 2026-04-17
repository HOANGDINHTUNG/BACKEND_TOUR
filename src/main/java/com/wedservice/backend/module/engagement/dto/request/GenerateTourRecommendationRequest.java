package com.wedservice.backend.module.engagement.dto.request;

import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateTourRecommendationRequest {

    @Size(max = 100, message = "requestedTag must not exceed 100 characters")
    private String requestedTag;

    private BudgetLevel requestedBudget;

    private PreferredTripMode requestedTripMode;

    @Min(value = 1, message = "requestedPeopleCount must be greater than or equal to 1")
    private Integer requestedPeopleCount;

    private LocalDateTime requestedDepartureAt;

    @Min(value = 1, message = "size must be greater than or equal to 1")
    @Max(value = 20, message = "size must be less than or equal to 20")
    @Builder.Default
    private Integer size = 10;
}
