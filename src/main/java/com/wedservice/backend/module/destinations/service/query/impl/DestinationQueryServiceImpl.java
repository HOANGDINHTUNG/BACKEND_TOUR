package com.wedservice.backend.module.destinations.service.query.impl;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.service.DestinationService;
import com.wedservice.backend.module.destinations.service.query.DestinationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationQueryServiceImpl implements DestinationQueryService {

    private final DestinationService destinationService;

    @Override
    public PageResponse<DestinationResponse> searchApprovedDestinations(DestinationSearchRequest request) {
        return destinationService.searchApprovedDestinations(request);
    }

    @Override
    public DestinationDetailResponse getApprovedDestinationByUuid(UUID uuid) {
        return destinationService.getApprovedDestinationByUuid(uuid);
    }
}
