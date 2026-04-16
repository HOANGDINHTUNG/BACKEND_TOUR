package com.wedservice.backend.module.tours.facade;

import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;
import com.wedservice.backend.module.tours.service.command.TourCommandService;
import com.wedservice.backend.module.tours.service.query.TourQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public List<TourScheduleResponse> getTourSchedules(Long tourId) {
        return tourQueryService.getTourSchedules(tourId);
    }

    public List<TourScheduleResponse> getAdminTourSchedules(Long tourId) {
        return tourQueryService.getAdminTourSchedules(tourId);
    }

    public TourScheduleResponse getTourSchedule(Long tourId, Long scheduleId) {
        return tourQueryService.getTourSchedule(tourId, scheduleId);
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

    public TourScheduleResponse createTourSchedule(Long tourId, TourScheduleRequest request) {
        return tourCommandService.createTourSchedule(tourId, request);
    }

    public TourScheduleResponse updateTourSchedule(Long tourId, Long scheduleId, TourScheduleRequest request) {
        return tourCommandService.updateTourSchedule(tourId, scheduleId, request);
    }

    public TourScheduleResponse updateTourScheduleStatus(Long tourId, Long scheduleId, String status) {
        return tourCommandService.updateTourScheduleStatus(tourId, scheduleId, status);
    }
}

