package com.wedservice.backend.module.weather.repository;

import com.wedservice.backend.module.weather.entity.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {

    List<WeatherForecast> findByDestinationIdOrderByForecastDateAsc(Long destinationId);

    List<WeatherForecast> findByDestinationIdAndForecastDateGreaterThanEqualOrderByForecastDateAsc(
            Long destinationId,
            LocalDate forecastDate
    );

    Optional<WeatherForecast> findByDestinationIdAndForecastDate(Long destinationId, LocalDate forecastDate);
}
