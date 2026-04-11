package com.wedservice.backend.module.tours.service.query.impl;

import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.service.query.TourQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourQueryServiceImpl implements TourQueryService {

    private final TourRepository tourRepository;

    @Override
    public Page<TourResponse> searchTours(TourSearchRequest request) {
        PageRequest pr = PageRequest.of(request.getPage(), request.getSize());
        List<TourResponse> list = tourRepository.findAll(pr).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(list, pr, tourRepository.count());
    }

    @Override
    public TourResponse getTour(Long id) {
        return tourRepository.findById(id).map(this::toResponse).orElseThrow(() -> new IllegalArgumentException("Tour not found"));
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
