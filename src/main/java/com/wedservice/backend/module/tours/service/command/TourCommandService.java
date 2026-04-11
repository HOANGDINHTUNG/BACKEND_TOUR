package com.wedservice.backend.module.tours.service.command;

import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;

public interface TourCommandService {
    TourResponse createTour(TourRequest request);
    TourResponse updateTour(Long id, TourRequest request);
    void deleteTour(Long id);
}
