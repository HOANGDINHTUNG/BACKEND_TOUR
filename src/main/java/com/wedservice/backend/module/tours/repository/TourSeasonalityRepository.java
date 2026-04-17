package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourSeasonality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourSeasonalityRepository extends JpaRepository<TourSeasonality, Long> {
    List<TourSeasonality> findByTourId(Long tourId);

    List<TourSeasonality> findByTourIdIn(List<Long> tourIds);

    @Query("""
            select ts
            from TourSeasonality ts
            where ts.deletedAt is null
              and ts.monthFrom is not null
              and ts.monthTo is not null
              and :travelMonth between ts.monthFrom and ts.monthTo
            """)
    List<TourSeasonality> findActiveByTravelMonth(@Param("travelMonth") Integer travelMonth);
}
