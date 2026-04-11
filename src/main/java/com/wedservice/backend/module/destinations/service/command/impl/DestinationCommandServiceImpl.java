package com.wedservice.backend.module.destinations.service.command.impl;

import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.service.DestinationService;
import com.wedservice.backend.module.destinations.service.command.DestinationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DestinationCommandServiceImpl implements DestinationCommandService {

    private final DestinationService destinationService;

    @Override
    public DestinationDetailResponse proposeDestination(DestinationRequest request) {
        return destinationService.proposeDestination(request);
    }
}
