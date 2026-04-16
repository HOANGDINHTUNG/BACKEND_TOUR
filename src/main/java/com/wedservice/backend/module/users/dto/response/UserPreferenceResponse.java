package com.wedservice.backend.module.users.dto.response;

import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.entity.TravelStyle;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserPreferenceResponse {
    private Long id;
    private BudgetLevel budgetLevel;
    private PreferredTripMode preferredTripMode;
    private TravelStyle travelStyle;
    private String preferredDepartureCity;
    private List<String> favoriteRegions;
    private List<String> favoriteTags;
    private List<String> favoriteDestinations;
    private Boolean prefersLowMobility;
    private Boolean prefersFamilyFriendly;
    private Boolean prefersStudentBudget;
    private Boolean prefersWeatherAlert;
    private Boolean prefersPromotionAlert;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
