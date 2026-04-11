package com.wedservice.backend.module.tours.service.query;

import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import org.springframework.data.domain.Page;

public interface TourQueryService {
    Page<TourResponse> searchTours(TourSearchRequest request);
    TourResponse getTour(Long id);
}
