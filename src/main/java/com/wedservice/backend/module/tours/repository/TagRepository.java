package com.wedservice.backend.module.tours.repository;

import com.wedservice.backend.module.tours.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByIdInAndIsActiveTrue(Collection<Long> ids);
}
