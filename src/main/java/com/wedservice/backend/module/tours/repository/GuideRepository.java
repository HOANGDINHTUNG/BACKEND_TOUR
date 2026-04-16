package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GuideRepository extends JpaRepository<Guide, Long> {

    List<Guide> findByIdIn(Collection<Long> ids);
}
