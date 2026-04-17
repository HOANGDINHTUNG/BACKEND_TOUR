package com.wedservice.backend.module.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrowdPredictionResponse {

    private Long id;
    private Long destinationId;
    private LocalDate predictionDate;
    private String crowdLevel;
    private Integer predictedVisitors;
    private BigDecimal confidenceScore;
    private String reasonsJson;
    private LocalDateTime createdAt;
}
