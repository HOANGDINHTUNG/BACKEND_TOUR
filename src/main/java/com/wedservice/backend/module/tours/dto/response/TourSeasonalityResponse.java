package com.wedservice.backend.module.tours.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSeasonalityResponse {

    private Long id;
    private String seasonName;
    private Integer monthFrom;
    private Integer monthTo;
    private BigDecimal recommendationScore;
    private String notes;
}
