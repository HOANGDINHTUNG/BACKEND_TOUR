package com.wedservice.backend.module.users.entity;

import com.wedservice.backend.module.users.entity.converter.BudgetLevelConverter;
import com.wedservice.backend.module.users.entity.converter.PreferredTripModeConverter;
import com.wedservice.backend.module.users.entity.converter.TravelStyleConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private UUID userId;

    @Convert(converter = BudgetLevelConverter.class)
    @Column(name = "budget_level", length = 20)
    private BudgetLevel budgetLevel;

    @Convert(converter = PreferredTripModeConverter.class)
    @Column(name = "preferred_trip_mode", length = 20)
    private PreferredTripMode preferredTripMode;

    @Convert(converter = TravelStyleConverter.class)
    @Column(name = "travel_style", length = 20)
    private TravelStyle travelStyle;

    @Column(name = "preferred_departure_city", length = 120)
    private String preferredDepartureCity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "favorite_regions", columnDefinition = "json")
    @Builder.Default
    private List<String> favoriteRegions = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "favorite_tags", columnDefinition = "json")
    @Builder.Default
    private List<String> favoriteTags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "favorite_destinations", columnDefinition = "json")
    @Builder.Default
    private List<String> favoriteDestinations = new ArrayList<>();

    @Column(name = "prefers_low_mobility", nullable = false)
    @Builder.Default
    private Boolean prefersLowMobility = false;

    @Column(name = "prefers_family_friendly", nullable = false)
    @Builder.Default
    private Boolean prefersFamilyFriendly = false;

    @Column(name = "prefers_student_budget", nullable = false)
    @Builder.Default
    private Boolean prefersStudentBudget = false;

    @Column(name = "prefers_weather_alert", nullable = false)
    @Builder.Default
    private Boolean prefersWeatherAlert = true;

    @Column(name = "prefers_promotion_alert", nullable = false)
    @Builder.Default
    private Boolean prefersPromotionAlert = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    protected void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        applyDefaults();
    }

    @jakarta.persistence.PreUpdate
    protected void beforeUpdate() {
        updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    private void applyDefaults() {
        if (favoriteRegions == null) {
            favoriteRegions = new ArrayList<>();
        }
        if (favoriteTags == null) {
            favoriteTags = new ArrayList<>();
        }
        if (favoriteDestinations == null) {
            favoriteDestinations = new ArrayList<>();
        }
        if (prefersLowMobility == null) {
            prefersLowMobility = false;
        }
        if (prefersFamilyFriendly == null) {
            prefersFamilyFriendly = false;
        }
        if (prefersStudentBudget == null) {
            prefersStudentBudget = false;
        }
        if (prefersWeatherAlert == null) {
            prefersWeatherAlert = true;
        }
        if (prefersPromotionAlert == null) {
            prefersPromotionAlert = true;
        }
    }
}
