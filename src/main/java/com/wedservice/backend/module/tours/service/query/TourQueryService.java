package com.wedservice.backend.module.tours.service.query;

import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TourQueryService {
    Page<TourResponse> searchTours(TourSearchRequest request);
    TourResponse getTour(Long id);
    List<TourScheduleResponse> getTourSchedules(Long tourId);
    List<TourScheduleResponse> getAdminTourSchedules(Long tourId);
    TourScheduleResponse getTourSchedule(Long tourId, Long scheduleId);
}
