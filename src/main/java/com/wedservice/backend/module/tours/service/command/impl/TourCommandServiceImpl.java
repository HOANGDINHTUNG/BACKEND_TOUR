package com.wedservice.backend.module.tours.service.command.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.service.command.TourCommandService;
import com.wedservice.backend.module.tours.validator.TourValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TourCommandServiceImpl implements TourCommandService {

    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private final TourValidator tourValidator;

    @Override
    @Transactional
    public TourResponse createTour(TourRequest request) {
        tourValidator.validateRequest(request);
        Destination destination = findDestination(request.getDestinationId());

        Tour t = Tour.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .slug(request.getSlug().trim())
                .destination(destination)
                .basePrice(request.getBasePrice())
                .currency(tourValidator.normalizeCurrency(request.getCurrency()))
                .durationDays(request.getDurationDays())
                .durationNights(request.getDurationNights() == null ? 0 : request.getDurationNights())
                .transportType(request.getTransportType())
                .tripMode(request.getTripMode())
                .highlights(request.getHighlights())
                .inclusions(request.getInclusions())
                .exclusions(request.getExclusions())
                .notes(request.getNotes())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .status(tourValidator.normalizeStatus(request.getStatus()))
                .build();

        t = tourRepository.save(t);
        return toResponse(t);
    }

    @Override
    @Transactional
    public TourResponse updateTour(Long id, TourRequest request) {
        tourValidator.validateRequest(request);
        Tour t = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        Destination destination = findDestination(request.getDestinationId());

        t.setCode(request.getCode().trim());
        t.setName(request.getName().trim());
        t.setSlug(request.getSlug().trim());
        t.setDestination(destination);
        t.setBasePrice(request.getBasePrice());
        t.setCurrency(tourValidator.normalizeCurrency(request.getCurrency()));
        t.setDurationDays(request.getDurationDays());
        t.setDurationNights(request.getDurationNights() == null ? 0 : request.getDurationNights());
        t.setTransportType(request.getTransportType());
        t.setTripMode(request.getTripMode());
        t.setHighlights(request.getHighlights());
        t.setInclusions(request.getInclusions());
        t.setExclusions(request.getExclusions());
        t.setNotes(request.getNotes());
        t.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        t.setStatus(tourValidator.normalizeStatus(request.getStatus()));

        t = tourRepository.save(t);
        return toResponse(t);
    }

    @Override
    public void deleteTour(Long id) {
        tourRepository.deleteById(id);
    }

    private Destination findDestination(Long destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));
        if (destination.getDeletedAt() != null) {
            throw new BadRequestException("Destination has been deleted");
        }
        return destination;
    }

    private TourResponse toResponse(Tour t) {
        return TourResponse.builder()
                .id(t.getId())
                .code(t.getCode())
                .name(t.getName())
                .slug(t.getSlug())
                .destinationId(t.getDestination() != null ? t.getDestination().getId() : null)
                .basePrice(t.getBasePrice())
                .currency(t.getCurrency())
                .build();
    }
}
