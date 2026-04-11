package com.wedservice.backend.module.tours.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourRequest {
    private String code;
    private String name;
    private String slug;
    private Long destinationId;
    private BigDecimal basePrice;
    private String currency;
    private Integer durationDays;
    private Integer durationNights;
    private String transportType;
    private String tripMode;
    private String highlights;
    private String inclusions;
    private String exclusions;
    private String notes;
    private Boolean isFeatured;
    private String status;
}
