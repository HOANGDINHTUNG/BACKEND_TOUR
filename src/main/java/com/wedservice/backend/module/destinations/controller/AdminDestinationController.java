package com.wedservice.backend.module.destinations.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.request.RejectProposalRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.facade.AdminDestinationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/destinations")
@RequiredArgsConstructor
public class AdminDestinationController {

    private final AdminDestinationFacade adminDestinationFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('destination.view')")
    public ApiResponse<PageResponse<DestinationResponse>> searchDestinations(DestinationSearchRequest request) {
    return ApiResponse.success(adminDestinationFacade.searchDestinations(request));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('destination.view')")
    public ApiResponse<DestinationDetailResponse> getDestination(@PathVariable UUID uuid) {
    return ApiResponse.success(adminDestinationFacade.getDestinationByUuid(uuid));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('destination.create')")
    public ApiResponse<DestinationDetailResponse> createDestination(@Valid @RequestBody DestinationRequest request) {
    return ApiResponse.success(adminDestinationFacade.createDestination(request), "Create destination successfully");
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('destination.update')")
    public ApiResponse<DestinationDetailResponse> updateDestination(
            @PathVariable UUID uuid,
            @Valid @RequestBody DestinationRequest request
    ) {
    return ApiResponse.success(adminDestinationFacade.updateDestination(uuid, request), "Update destination successfully");
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('destination.delete')")
    public ApiResponse<Void> deleteDestination(@PathVariable UUID uuid) {
    adminDestinationFacade.deleteDestination(uuid);
        return ApiResponse.success(null, "Delete destination successfully");
    }

    @PatchMapping("/{uuid}/approve")
    @PreAuthorize("hasAnyAuthority('destination.review','destination.publish')")
    public ApiResponse<DestinationDetailResponse> approveProposal(@PathVariable UUID uuid) {
    return ApiResponse.success(adminDestinationFacade.approveProposal(uuid), "Approve proposal successfully");
    }

    @PatchMapping("/{uuid}/reject")
    @PreAuthorize("hasAnyAuthority('destination.review','destination.publish')")
    public ApiResponse<DestinationDetailResponse> rejectProposal(
            @PathVariable UUID uuid,
            @Valid @RequestBody RejectProposalRequest request
    ) {
    return ApiResponse.success(adminDestinationFacade.rejectProposal(uuid, request), "Reject proposal successfully");
    }
}
