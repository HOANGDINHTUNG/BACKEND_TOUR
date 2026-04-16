package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {

    List<TourSchedule> findByTourId(Long tourId);

    Optional<TourSchedule> findByIdAndTourId(Long id, Long tourId);

}
