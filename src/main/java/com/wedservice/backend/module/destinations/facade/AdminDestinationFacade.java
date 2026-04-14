package com.wedservice.backend.module.destinations.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.request.RejectProposalRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.service.command.AdminDestinationCommandService;
import com.wedservice.backend.module.destinations.service.query.AdminDestinationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminDestinationFacade {

    private final AdminDestinationCommandService commandService;
    private final AdminDestinationQueryService queryService;

    public PageResponse<DestinationResponse> searchDestinations(DestinationSearchRequest request) {
        return queryService.searchDestinations(request);
    }

    public DestinationDetailResponse getDestinationByUuid(UUID uuid) {
        return queryService.getDestinationByUuid(uuid);
    }

    public DestinationDetailResponse createDestination(DestinationRequest request) {
        return commandService.createDestination(request);
    }

    public DestinationDetailResponse updateDestination(UUID uuid, DestinationRequest request) {
        return commandService.updateDestination(uuid, request);
    }

    public void deleteDestination(UUID uuid) {
        commandService.deleteDestination(uuid);
    }

    public DestinationDetailResponse approveProposal(UUID uuid) {
        return commandService.approveProposal(uuid);
    }

    public DestinationDetailResponse rejectProposal(UUID uuid, RejectProposalRequest request) {
        return commandService.rejectProposal(uuid, request);
    }
}
