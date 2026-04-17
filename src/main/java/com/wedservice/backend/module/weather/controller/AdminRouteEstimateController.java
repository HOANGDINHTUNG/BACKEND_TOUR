package com.wedservice.backend.module.weather.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.weather.dto.request.AdminCreateRouteEstimateRequest;
import com.wedservice.backend.module.weather.dto.response.RouteEstimateResponse;
import com.wedservice.backend.module.weather.facade.AdminWeatherFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/route-estimates")
@RequiredArgsConstructor
public class AdminRouteEstimateController {

    private final AdminWeatherFacade adminWeatherFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('destination.view')")
    public ApiResponse<List<RouteEstimateResponse>> getRouteEstimates(
            @RequestParam(required = false) String fromLabel,
            @RequestParam(required = false) String toLabel
    ) {
        return ApiResponse.<List<RouteEstimateResponse>>builder()
                .success(true)
                .message("Route estimates fetched successfully")
                .data(adminWeatherFacade.getRouteEstimates(fromLabel, toLabel))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('destination.update')")
    public ApiResponse<RouteEstimateResponse> createRouteEstimate(
            @Valid @RequestBody AdminCreateRouteEstimateRequest request
    ) {
        return ApiResponse.<RouteEstimateResponse>builder()
                .success(true)
                .message("Route estimate created successfully")
                .data(adminWeatherFacade.createRouteEstimate(request))
                .build();
    }
}
