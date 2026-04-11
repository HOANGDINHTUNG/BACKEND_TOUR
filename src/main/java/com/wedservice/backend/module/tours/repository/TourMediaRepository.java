package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourMediaRepository extends JpaRepository<TourMedia, Long> {
    List<TourMedia> findByTourIdOrderBySortOrder(Long tourId);
}
