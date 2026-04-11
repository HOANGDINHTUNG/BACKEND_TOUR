package com.wedservice.backend.module.tours.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.facade.TourFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tours")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTourController {

    private final TourFacade tourFacade;

    @PostMapping
    public ApiResponse<TourResponse> createTour(@Validated @RequestBody TourRequest request) {
    return ApiResponse.success(tourFacade.createTour(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TourResponse> updateTour(@PathVariable Long id, @Validated @RequestBody TourRequest request) {
    return ApiResponse.success(tourFacade.updateTour(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTour(@PathVariable Long id) {
    tourFacade.deleteTour(id);
        return ApiResponse.success("Deleted");
    }
}
