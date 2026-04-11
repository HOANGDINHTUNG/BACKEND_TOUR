package com.wedservice.backend.module.tours.service.command.impl;

import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.service.command.TourCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TourCommandServiceImpl implements TourCommandService {

    private final TourRepository tourRepository;

    @Override
    public TourResponse createTour(TourRequest request) {
        Tour t = Tour.builder()
                .code(request.getCode())
                .name(request.getName())
                .slug(request.getSlug())
                .basePrice(request.getBasePrice())
                .currency(request.getCurrency())
                .durationDays(request.getDurationDays())
                .durationNights(request.getDurationNights())
                .transportType(request.getTransportType())
                .tripMode(request.getTripMode())
                .highlights(request.getHighlights())
                .inclusions(request.getInclusions())
                .exclusions(request.getExclusions())
                .notes(request.getNotes())
                .isFeatured(request.getIsFeatured())
                .status(request.getStatus())
                .build();

        t = tourRepository.save(t);

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

    @Override
    public TourResponse updateTour(Long id, TourRequest request) {
        Tour t = tourRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tour not found"));

        t.setName(request.getName());
        t.setSlug(request.getSlug());
        t.setBasePrice(request.getBasePrice());
        t.setCurrency(request.getCurrency());
        t.setDurationDays(request.getDurationDays());
        t.setDurationNights(request.getDurationNights());
        t.setTransportType(request.getTransportType());
        t.setTripMode(request.getTripMode());
        t.setHighlights(request.getHighlights());
        t.setInclusions(request.getInclusions());
        t.setExclusions(request.getExclusions());
        t.setNotes(request.getNotes());
        t.setIsFeatured(request.getIsFeatured());
        t.setStatus(request.getStatus());

        t = tourRepository.save(t);

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

    @Override
    public void deleteTour(Long id) {
        tourRepository.deleteById(id);
    }
}
