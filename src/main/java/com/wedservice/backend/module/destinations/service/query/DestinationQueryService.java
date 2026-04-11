package com.wedservice.backend.module.destinations.service.query;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;

import java.util.UUID;

public interface DestinationQueryService {
    PageResponse<DestinationResponse> searchApprovedDestinations(DestinationSearchRequest request);
    DestinationDetailResponse getApprovedDestinationByUuid(UUID uuid);
}
