package com.wedservice.backend.module.destinations.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.FollowDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationFollowResponse;
import com.wedservice.backend.module.destinations.service.command.DestinationFollowCommandService;
import com.wedservice.backend.module.destinations.service.query.DestinationFollowQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DestinationFollowFacade {

    private final DestinationFollowCommandService commandService;
    private final DestinationFollowQueryService queryService;

    public DestinationFollowResponse followDestination(UUID destinationUuid, FollowDestinationRequest request) {
        return commandService.followDestination(destinationUuid, request);
    }

    public void unfollowDestination(UUID destinationUuid) {
        commandService.unfollowDestination(destinationUuid);
    }

    public DestinationFollowResponse updateFollowSettings(UUID destinationUuid, FollowDestinationRequest request) {
        return commandService.updateFollowSettings(destinationUuid, request);
    }

    public PageResponse<DestinationFollowResponse> getMyFollows(int page, int size) {
        return queryService.getMyFollows(page, size);
    }
}
