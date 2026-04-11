package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.TourChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourChecklistItemRepository extends JpaRepository<TourChecklistItem, Long> {
    List<TourChecklistItem> findByTourId(Long tourId);
}
