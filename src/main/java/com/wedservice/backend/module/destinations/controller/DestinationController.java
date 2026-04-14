package com.wedservice.backend.module.destinations.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.request.ProposeDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationProposalResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicResponse;
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
    public ApiResponse<PageResponse<DestinationPublicResponse>> searchDestinations(DestinationSearchRequest request) {
    return ApiResponse.success(destinationFacade.searchApprovedDestinations(request));
    }

    @GetMapping("/{uuid}")
    public ApiResponse<DestinationPublicDetailResponse> getDestination(@PathVariable UUID uuid) {
    return ApiResponse.success(destinationFacade.getApprovedDestinationByUuid(uuid));
    }

    @PostMapping("/propose")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('destination.propose','destination.create')")
    public ApiResponse<DestinationProposalResponse> proposeDestination(@Valid @RequestBody ProposeDestinationRequest request) {
    return ApiResponse.success(destinationFacade.proposeDestination(request), "Propose destination successfully, please wait for admin review");
    }
}
