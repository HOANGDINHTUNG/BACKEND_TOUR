package com.wedservice.backend.module.tours.service.query.impl;

import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.service.query.TourQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.tours.entity.QTour;
import com.querydsl.core.BooleanBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TourQueryServiceImpl implements TourQueryService {

    private final TourRepository tourRepository;

    @Override
    @Cacheable(value = "tours", key = "#request")
    public Page<TourResponse> searchTours(TourSearchRequest request) {
        PageRequest pr = PageRequest.of(request.getPage(), request.getSize());
        
        QTour qTour = QTour.tour;
        BooleanBuilder builder = new BooleanBuilder();
        
        // Filter out soft-deleted tours
        builder.and(qTour.deletedAt.isNull());
        
        // Use repo with QueryDSL
        Page<Tour> page = tourRepository.findAll(builder, pr);
        
        return page.map(this::toResponse);
    }

    @Override
    @Cacheable(value = "tour-details", key = "#id")
    public TourResponse getTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        
        if (tour.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Tour has been deleted");
        }
        
        return toResponse(tour);
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
