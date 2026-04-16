package com.wedservice.backend.module.tours.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class TourSeasonalityRequest {

    @NotBlank
    @Size(max = 100)
    private String seasonName;

    @Min(1)
    @Max(12)
    private Integer monthFrom;

    @Min(1)
    @Max(12)
    private Integer monthTo;

    @DecimalMin(value = "0.0")
    @Builder.Default
    private BigDecimal recommendationScore = BigDecimal.ZERO;

    private String notes;
}
