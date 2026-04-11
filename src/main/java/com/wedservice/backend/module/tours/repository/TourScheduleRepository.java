package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {

    List<TourSchedule> findByTourId(Long tourId);

}
