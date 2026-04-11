package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourSchedulePickupPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourSchedulePickupPointRepository extends JpaRepository<TourSchedulePickupPoint, Long> {
    List<TourSchedulePickupPoint> findByScheduleIdOrderBySortOrder(Long scheduleId);
}
