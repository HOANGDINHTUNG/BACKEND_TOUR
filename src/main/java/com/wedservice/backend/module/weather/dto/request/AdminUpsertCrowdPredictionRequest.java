package com.wedservice.backend.module.weather.dto.request;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import jakarta.validation.constraints.Min;
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
public class AdminUpsertCrowdPredictionRequest {

    private CrowdLevel crowdLevel;

    @Min(0)
    private Integer predictedVisitors;

    private BigDecimal confidenceScore;

    @Size(max = 4000)
    private String reasonsJson;
}
