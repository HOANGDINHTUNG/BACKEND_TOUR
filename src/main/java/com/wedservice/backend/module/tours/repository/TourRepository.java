package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<Tour, Long> {

    boolean existsByCode(String code);

    boolean existsBySlug(String slug);
}
