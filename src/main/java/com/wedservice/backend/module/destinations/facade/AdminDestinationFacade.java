package com.wedservice.backend.module.destinations.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.request.RejectProposalRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.service.AdminDestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminDestinationFacade {

    private final AdminDestinationService adminDestinationService;

    public PageResponse<DestinationResponse> searchDestinations(DestinationSearchRequest request) {
        return adminDestinationService.searchDestinations(request);
    }

    public DestinationDetailResponse getDestinationByUuid(UUID uuid) {
        return adminDestinationService.getDestinationByUuid(uuid);
    }

    public DestinationDetailResponse createDestination(DestinationRequest request) {
        return adminDestinationService.createDestination(request);
    }

    public DestinationDetailResponse updateDestination(UUID uuid, DestinationRequest request) {
        return adminDestinationService.updateDestination(uuid, request);
    }

    public void deleteDestination(UUID uuid) {
        adminDestinationService.deleteDestination(uuid);
    }

    public DestinationDetailResponse approveProposal(UUID uuid) {
        return adminDestinationService.approveProposal(uuid);
    }

    public DestinationDetailResponse rejectProposal(UUID uuid, RejectProposalRequest request) {
        return adminDestinationService.rejectProposal(uuid, request);
    }
}
