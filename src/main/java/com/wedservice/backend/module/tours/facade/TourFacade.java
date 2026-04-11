package com.wedservice.backend.module.tours.facade;

import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.service.command.TourCommandService;
import com.wedservice.backend.module.tours.service.query.TourQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TourFacade {

    private final TourCommandService tourCommandService;
    private final TourQueryService tourQueryService;

    public Page<TourResponse> searchTours(TourSearchRequest request) {
        return tourQueryService.searchTours(request);
    }

    public TourResponse getTour(Long id) {
        return tourQueryService.getTour(id);
    }

    public TourResponse createTour(TourRequest request) {
        return tourCommandService.createTour(request);
    }

    public TourResponse updateTour(Long id, TourRequest request) {
        return tourCommandService.updateTour(id, request);
    }

    public void deleteTour(Long id) {
        tourCommandService.deleteTour(id);
    }
}

