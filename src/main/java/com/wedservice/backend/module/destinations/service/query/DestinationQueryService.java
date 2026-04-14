package com.wedservice.backend.module.destinations.service.query;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicResponse;

import java.util.UUID;

public interface DestinationQueryService {
    PageResponse<DestinationPublicResponse> searchApprovedDestinations(DestinationSearchRequest request);
    DestinationPublicDetailResponse getApprovedDestinationByUuid(UUID uuid);
}
