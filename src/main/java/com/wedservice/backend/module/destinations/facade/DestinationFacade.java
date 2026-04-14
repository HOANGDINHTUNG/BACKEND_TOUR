package com.wedservice.backend.module.destinations.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.request.ProposeDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationProposalResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicResponse;
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

    public PageResponse<DestinationPublicResponse> searchApprovedDestinations(DestinationSearchRequest request) {
        return queryService.searchApprovedDestinations(request);
    }

    public DestinationPublicDetailResponse getApprovedDestinationByUuid(UUID uuid) {
        return queryService.getApprovedDestinationByUuid(uuid);
    }

    public DestinationProposalResponse proposeDestination(ProposeDestinationRequest request) {
        validator.validatePropose(request);
        return commandService.proposeDestination(request);
    }
}
