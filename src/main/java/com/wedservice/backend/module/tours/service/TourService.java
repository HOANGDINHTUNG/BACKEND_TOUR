package com.wedservice.backend.module.tours.service;

import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import org.springframework.data.domain.Page;

public interface TourService {
    Page<TourResponse> searchTours(TourSearchRequest request);

    TourResponse getTour(Long id);

    TourResponse createTour(TourRequest request);

    TourResponse updateTour(Long id, TourRequest request);

    void deleteTour(Long id);
}
