package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourTag;
import com.wedservice.backend.module.tours.entity.TourTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TourTagRepository extends JpaRepository<TourTag, TourTagId> {

    List<TourTag> findByIdTourId(Long tourId);

    List<TourTag> findByIdTourIdIn(Collection<Long> tourIds);

    List<TourTag> findByIdTagIdIn(Collection<Long> tagIds);
}
