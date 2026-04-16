package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSearchRequest {
    private Long destinationId;

    @Size(max = 100, message = "keyword must not exceed 100 characters")
    private String keyword;

    private List<Long> tagIds;

    @DecimalMin(value = "0.0", inclusive = true, message = "minPrice must be greater than or equal to 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "maxPrice must be greater than or equal to 0")
    private BigDecimal maxPrice;

    @Min(value = 1, message = "travelMonth must be between 1 and 12")
    @Max(value = 12, message = "travelMonth must be between 1 and 12")
    private Integer travelMonth;

    private Boolean featuredOnly;

    private Boolean studentFriendlyOnly;

    private Boolean familyFriendlyOnly;

    private Boolean seniorFriendlyOnly;

    @Min(value = 1, message = "difficultyLevel must be between 1 and 5")
    @Max(value = 5, message = "difficultyLevel must be between 1 and 5")
    private Integer difficultyLevel;

    @Min(value = 1, message = "activityLevel must be between 1 and 5")
    @Max(value = 5, message = "activityLevel must be between 1 and 5")
    private Integer activityLevel;

    @Min(value = 1, message = "minDurationDays must be greater than or equal to 1")
    private Integer minDurationDays;

    @Min(value = 1, message = "maxDurationDays must be greater than or equal to 1")
    private Integer maxDurationDays;

    @Min(value = 0, message = "travellerAge must be greater than or equal to 0")
    private Integer travellerAge;

    @Min(value = 1, message = "groupSize must be greater than or equal to 1")
    private Integer groupSize;

    @Size(max = 120, message = "transportType must not exceed 120 characters")
    private String transportType;

    @Pattern(
            regexp = "group|private|shared",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "tripMode must be one of group, private, shared"
    )
    private String tripMode;

    @DecimalMin(value = "0.0", inclusive = true, message = "minRating must be greater than or equal to 0")
    @jakarta.validation.constraints.DecimalMax(value = "5.0", inclusive = true, message = "minRating must be less than or equal to 5")
    private BigDecimal minRating;

    @Pattern(
            regexp = "name|basePrice|durationDays|averageRating|totalBookings|createdAt",
            message = "sortBy is invalid"
    )
    @Builder.Default
    private String sortBy = "createdAt";

    @Pattern(
            regexp = "asc|desc",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "sortDir must be asc or desc"
    )
    @Builder.Default
    private String sortDir = "desc";

    @Min(value = 0, message = "page must be greater than or equal to 0")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "size must be greater than or equal to 1")
    @Max(value = 100, message = "size must be less than or equal to 100")
    @Builder.Default
    private Integer size = 10;
}
