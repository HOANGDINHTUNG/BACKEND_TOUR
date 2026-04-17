package com.wedservice.backend.module.weather.repository;

import com.wedservice.backend.module.weather.entity.CrowdPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CrowdPredictionRepository extends JpaRepository<CrowdPrediction, Long> {

    List<CrowdPrediction> findByDestinationIdAndPredictionDateGreaterThanEqualOrderByPredictionDateAsc(
            Long destinationId,
            LocalDate predictionDate
    );

    List<CrowdPrediction> findByDestinationIdOrderByPredictionDateAsc(Long destinationId);

    Optional<CrowdPrediction> findByDestinationIdAndPredictionDate(Long destinationId, LocalDate predictionDate);
}
