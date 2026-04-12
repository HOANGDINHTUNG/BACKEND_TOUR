package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TourRepository extends JpaRepository<Tour, Long>, QuerydslPredicateExecutor<Tour> {

    boolean existsByCode(String code);

    boolean existsBySlug(String slug);
}
