package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourScheduleGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourScheduleGuideRepository extends JpaRepository<TourScheduleGuide, Long> {
    List<TourScheduleGuide> findByScheduleId(Long scheduleId);
}
