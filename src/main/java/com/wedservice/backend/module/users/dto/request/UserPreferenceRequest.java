package com.wedservice.backend.module.users.dto.request;

import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.entity.TravelStyle;
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
public class UserPreferenceRequest {

    private BudgetLevel budgetLevel;
    private PreferredTripMode preferredTripMode;
    private TravelStyle travelStyle;

    @Size(max = 120, message = "preferredDepartureCity must not exceed 120 characters")
    private String preferredDepartureCity;

    private List<String> favoriteRegions;
    private List<String> favoriteTags;
    private List<String> favoriteDestinations;

    private Boolean prefersLowMobility;
    private Boolean prefersFamilyFriendly;
    private Boolean prefersStudentBudget;
    private Boolean prefersWeatherAlert;
    private Boolean prefersPromotionAlert;
}
