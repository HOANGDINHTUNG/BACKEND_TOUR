package com.wedservice.backend.module.destinations.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.facade.DestinationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationFacade destinationFacade;

    @GetMapping
    public ApiResponse<PageResponse<DestinationResponse>> searchDestinations(DestinationSearchRequest request) {
    return ApiResponse.success(destinationFacade.searchApprovedDestinations(request));
    }

    @GetMapping("/{uuid}")
    public ApiResponse<DestinationDetailResponse> getDestination(@PathVariable UUID uuid) {
    return ApiResponse.success(destinationFacade.getApprovedDestinationByUuid(uuid));
    }

    @PostMapping("/propose")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DestinationDetailResponse> proposeDestination(@Valid @RequestBody DestinationRequest request) {
    return ApiResponse.success(destinationFacade.proposeDestination(request), "Propose destination successfully, please wait for admin review");
    }
}
