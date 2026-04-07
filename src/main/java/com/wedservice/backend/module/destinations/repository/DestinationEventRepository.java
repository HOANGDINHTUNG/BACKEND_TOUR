package com.wedservice.backend.module.destinations.repository;

import com.wedservice.backend.module.destinations.entity.DestinationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinationEventRepository extends JpaRepository<DestinationEvent, Long> {

    List<DestinationEvent> findByDestinationIdAndIsActiveTrue(Long destinationId);
}
