package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {
    List<ItineraryItem> findByItineraryDayIdOrderBySequenceNo(Long itineraryDayId);
}
