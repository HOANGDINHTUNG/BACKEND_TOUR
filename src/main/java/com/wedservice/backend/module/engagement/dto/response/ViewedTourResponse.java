package com.wedservice.backend.module.engagement.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ViewedTourResponse {
    private Long viewId;
    private Long tourId;
    private String tourCode;
    private String tourName;
    private String tourSlug;
    private Long destinationId;
    private BigDecimal basePrice;
    private String currency;
    private Integer durationDays;
    private Integer durationNights;
    private String shortDescription;
    private Boolean isFeatured;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalBookings;
    private LocalDateTime viewedAt;
}
