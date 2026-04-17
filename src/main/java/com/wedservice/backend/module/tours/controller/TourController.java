package com.wedservice.backend.module.tours.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.engagement.facade.UserTourViewFacade;
import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;
import com.wedservice.backend.module.tours.facade.TourFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourFacade tourFacade;
    private final UserTourViewFacade userTourViewFacade;

    @GetMapping
    public ApiResponse<PageResponse<TourResponse>> searchTours(@Valid @ModelAttribute TourSearchRequest request) {
        var page = tourFacade.searchTours(request);
        PageResponse<TourResponse> resp = PageResponse.of(page);
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<TourResponse> getTour(@PathVariable Long id) {
        TourResponse response = tourFacade.getTour(id);
        userTourViewFacade.recordCurrentUserTourViewIfAuthenticated(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/schedules")
    public ApiResponse<List<TourScheduleResponse>> getTourSchedules(@PathVariable Long id) {
        return ApiResponse.success(tourFacade.getTourSchedules(id));
    }

    @GetMapping("/{tourId}/schedules/{scheduleId}")
    public ApiResponse<TourScheduleResponse> getTourSchedule(
            @PathVariable Long tourId,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.success(tourFacade.getTourSchedule(tourId, scheduleId));
    }
}
