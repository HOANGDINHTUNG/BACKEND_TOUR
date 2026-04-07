package com.wedservice.backend.module.destinations.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.service.AdminDestinationService;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminDestinationController {

    private final AdminDestinationService adminDestinationService;

    @GetMapping
    public ApiResponse<PageResponse<DestinationResponse>> searchDestinations(DestinationSearchRequest request) {
        return ApiResponse.success(adminDestinationService.searchDestinations(request));
    }

    @GetMapping("/{uuid}")
    public ApiResponse<DestinationResponse> getDestination(@PathVariable UUID uuid) {
        return ApiResponse.success(adminDestinationService.getDestinationByUuid(uuid));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DestinationResponse> createDestination(@Valid @RequestBody DestinationRequest request) {
        return ApiResponse.success(adminDestinationService.createDestination(request), "Create destination successfully");
    }

    @PutMapping("/{uuid}")
    public ApiResponse<DestinationResponse> updateDestination(
            @PathVariable UUID uuid,
            @Valid @RequestBody DestinationRequest request
    ) {
        return ApiResponse.success(adminDestinationService.updateDestination(uuid, request), "Update destination successfully");
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteDestination(@PathVariable UUID uuid) {
        adminDestinationService.deleteDestination(uuid);
        return ApiResponse.success(null, "Delete destination successfully");
    }

    @PatchMapping("/{uuid}/approve")
    public ApiResponse<DestinationResponse> approveProposal(@PathVariable UUID uuid) {
        return ApiResponse.success(adminDestinationService.approveProposal(uuid), "Approve proposal successfully");
    }

    @PatchMapping("/{uuid}/reject")
    public ApiResponse<DestinationResponse> rejectProposal(
            @PathVariable UUID uuid,
            @RequestBody String rejectionReason
    ) {
        return ApiResponse.success(adminDestinationService.rejectProposal(uuid, rejectionReason), "Reject proposal successfully");
    }
}
