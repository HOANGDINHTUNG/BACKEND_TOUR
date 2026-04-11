package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourSeasonality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourSeasonalityRepository extends JpaRepository<TourSeasonality, Long> {
    List<TourSeasonality> findByTourId(Long tourId);
}
