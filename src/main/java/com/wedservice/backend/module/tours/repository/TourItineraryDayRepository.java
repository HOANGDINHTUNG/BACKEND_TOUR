package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourItineraryDayRepository extends JpaRepository<TourItineraryDay, Long> {
    List<TourItineraryDay> findByTourIdOrderByDayNumber(Long tourId);
}
