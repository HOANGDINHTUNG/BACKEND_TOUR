package com.wedservice.backend.module.destinations.service.command;

import com.wedservice.backend.module.destinations.dto.request.FollowDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationFollowResponse;

import java.util.UUID;

public interface DestinationFollowCommandService {

    DestinationFollowResponse followDestination(UUID destinationUuid, FollowDestinationRequest request);

    void unfollowDestination(UUID destinationUuid);

    DestinationFollowResponse updateFollowSettings(UUID destinationUuid, FollowDestinationRequest request);
}
