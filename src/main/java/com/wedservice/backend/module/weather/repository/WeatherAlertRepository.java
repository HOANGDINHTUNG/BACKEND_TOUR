package com.wedservice.backend.module.weather.repository;

import com.wedservice.backend.module.weather.entity.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    List<WeatherAlert> findByDestinationIdOrderByValidFromDesc(Long destinationId);

    List<WeatherAlert> findByDestinationIdAndIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByValidFromDesc(
            Long destinationId,
            LocalDateTime validFrom,
            LocalDateTime validTo
    );

    Optional<WeatherAlert> findByIdAndDestinationId(Long id, Long destinationId);
}
