package com.wedservice.backend.module.destinations.service.query;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationFollowResponse;

public interface DestinationFollowQueryService {

    PageResponse<DestinationFollowResponse> getMyFollows(int page, int size);
}
