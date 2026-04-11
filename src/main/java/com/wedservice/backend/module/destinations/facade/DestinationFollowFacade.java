package com.wedservice.backend.module.destinations.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.FollowDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationFollowResponse;
import com.wedservice.backend.module.destinations.service.DestinationFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DestinationFollowFacade {

    private final DestinationFollowService followService;

    public DestinationFollowResponse followDestination(UUID destinationUuid, FollowDestinationRequest request) {
        return followService.followDestination(destinationUuid, request);
    }

    public void unfollowDestination(UUID destinationUuid) {
        followService.unfollowDestination(destinationUuid);
    }

    public DestinationFollowResponse updateFollowSettings(UUID destinationUuid, FollowDestinationRequest request) {
        return followService.updateFollowSettings(destinationUuid, request);
    }

    public PageResponse<DestinationFollowResponse> getMyFollows(int page, int size) {
        return followService.getMyFollows(page, size);
    }
}
