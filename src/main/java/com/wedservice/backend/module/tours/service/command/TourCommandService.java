package com.wedservice.backend.module.tours.service.command;

import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;

public interface TourCommandService {
    TourResponse createTour(TourRequest request);
    TourResponse updateTour(Long id, TourRequest request);
    void deleteTour(Long id);
    TourScheduleResponse createTourSchedule(Long tourId, TourScheduleRequest request);
    TourScheduleResponse updateTourSchedule(Long tourId, Long scheduleId, TourScheduleRequest request);
    TourScheduleResponse updateTourScheduleStatus(Long tourId, Long scheduleId, String status);
}
