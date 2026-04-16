package com.wedservice.backend.module.tours.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleRequest;
import com.wedservice.backend.module.tours.dto.request.UpdateTourScheduleStatusRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;
import com.wedservice.backend.module.tours.facade.TourFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/tours")
@RequiredArgsConstructor
public class AdminTourController {

    private final TourFacade tourFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('tour.create')")
    public ApiResponse<TourResponse> createTour(@Validated @RequestBody TourRequest request) {
        return ApiResponse.success(tourFacade.createTour(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tour.update')")
    public ApiResponse<TourResponse> updateTour(@PathVariable Long id, @Validated @RequestBody TourRequest request) {
        return ApiResponse.success(tourFacade.updateTour(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tour.delete')")
    public ApiResponse<String> deleteTour(@PathVariable Long id) {
        tourFacade.deleteTour(id);
        return ApiResponse.success("Deleted");
    }

    @GetMapping("/{tourId}/schedules")
    @PreAuthorize("hasAuthority('schedule.view')")
    public ApiResponse<List<TourScheduleResponse>> getAdminTourSchedules(@PathVariable Long tourId) {
        return ApiResponse.success(tourFacade.getAdminTourSchedules(tourId));
    }

    @GetMapping("/{tourId}/schedules/{scheduleId}")
    @PreAuthorize("hasAuthority('schedule.view')")
    public ApiResponse<TourScheduleResponse> getAdminTourSchedule(
            @PathVariable Long tourId,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.success(tourFacade.getTourSchedule(tourId, scheduleId));
    }

    @PostMapping("/{tourId}/schedules")
    @PreAuthorize("hasAuthority('schedule.create')")
    public ApiResponse<TourScheduleResponse> createTourSchedule(
            @PathVariable Long tourId,
            @Validated @RequestBody TourScheduleRequest request
    ) {
        return ApiResponse.success(tourFacade.createTourSchedule(tourId, request), "Tour schedule created");
    }

    @PutMapping("/{tourId}/schedules/{scheduleId}")
    @PreAuthorize("hasAuthority('schedule.update')")
    public ApiResponse<TourScheduleResponse> updateTourSchedule(
            @PathVariable Long tourId,
            @PathVariable Long scheduleId,
            @Validated @RequestBody TourScheduleRequest request
    ) {
        return ApiResponse.success(tourFacade.updateTourSchedule(tourId, scheduleId, request), "Tour schedule updated");
    }

    @PatchMapping("/{tourId}/schedules/{scheduleId}/status")
    @PreAuthorize("hasAuthority('schedule.close')")
    public ApiResponse<TourScheduleResponse> updateTourScheduleStatus(
            @PathVariable Long tourId,
            @PathVariable Long scheduleId,
            @Validated @RequestBody UpdateTourScheduleStatusRequest request
    ) {
        return ApiResponse.success(
                tourFacade.updateTourScheduleStatus(tourId, scheduleId, request.getStatus()),
                "Tour schedule status updated"
        );
    }
}
