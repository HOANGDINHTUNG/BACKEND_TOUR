package com.wedservice.backend.module.destinations.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.service.command.DestinationCommandService;
import com.wedservice.backend.module.destinations.service.query.DestinationQueryService;
import com.wedservice.backend.module.destinations.validator.DestinationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DestinationFacade {

    private final DestinationCommandService commandService;
    private final DestinationQueryService queryService;
    private final DestinationValidator validator;

    public PageResponse<DestinationResponse> searchApprovedDestinations(DestinationSearchRequest request) {
        return queryService.searchApprovedDestinations(request);
    }

    public DestinationDetailResponse getApprovedDestinationByUuid(UUID uuid) {
        return queryService.getApprovedDestinationByUuid(uuid);
    }

    public DestinationDetailResponse proposeDestination(DestinationRequest request) {
        validator.validatePropose(request);
        return commandService.proposeDestination(request);
    }
}
