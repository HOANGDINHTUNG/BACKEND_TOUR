package com.wedservice.backend.module.engagement.dto.response;

import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationLogResponse {

    private Long logId;
    private String requestedTag;
    private BudgetLevel requestedBudget;
    private PreferredTripMode requestedTripMode;
    private Integer requestedPeopleCount;
    private LocalDateTime requestedDepartureAt;
    private List<RecommendedTourResponse> recommendations;
    private LocalDateTime createdAt;
}
