package com.wedservice.backend.module.destinations.service.command;

import com.wedservice.backend.module.destinations.dto.request.ProposeDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationProposalResponse;

public interface DestinationCommandService {
    DestinationProposalResponse proposeDestination(ProposeDestinationRequest request);
}
