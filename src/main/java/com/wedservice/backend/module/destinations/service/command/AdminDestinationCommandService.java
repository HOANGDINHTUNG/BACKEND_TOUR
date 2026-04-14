package com.wedservice.backend.module.destinations.service.command;

import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.RejectProposalRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;

import java.util.UUID;

public interface AdminDestinationCommandService {

    DestinationDetailResponse createDestination(DestinationRequest request);

    DestinationDetailResponse updateDestination(UUID uuid, DestinationRequest request);

    void deleteDestination(UUID uuid);

    DestinationDetailResponse approveProposal(UUID uuid);

    DestinationDetailResponse rejectProposal(UUID uuid, RejectProposalRequest request);
}
