package com.wedservice.backend.module.destinations.service.command;

import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;

public interface DestinationCommandService {
    DestinationDetailResponse proposeDestination(DestinationRequest request);
}
