package com.wedservice.backend.module.weather.repository;

import com.wedservice.backend.module.weather.entity.RouteEstimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteEstimateRepository extends JpaRepository<RouteEstimate, Long> {

    List<RouteEstimate> findTop100ByOrderByCreatedAtDesc();
}
