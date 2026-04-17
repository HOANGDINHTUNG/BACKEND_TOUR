package com.wedservice.backend.module.weather.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.weather.dto.response.RouteEstimateResponse;
import com.wedservice.backend.module.weather.facade.WeatherFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/route-estimates")
@RequiredArgsConstructor
public class RouteEstimateController {

    private final WeatherFacade weatherFacade;

    @GetMapping
    public ApiResponse<List<RouteEstimateResponse>> getRouteEstimates(
            @RequestParam(required = false) String fromLabel,
            @RequestParam(required = false) String toLabel
    ) {
        return ApiResponse.<List<RouteEstimateResponse>>builder()
                .success(true)
                .message("Route estimates fetched successfully")
                .data(weatherFacade.getRouteEstimates(fromLabel, toLabel))
                .build();
    }
}
