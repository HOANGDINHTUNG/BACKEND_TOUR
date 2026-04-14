package com.wedservice.backend.module.destinations.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.destinations.dto.request.FollowDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationFollowResponse;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationFollow;
import com.wedservice.backend.module.destinations.mapper.DestinationMapper;
import com.wedservice.backend.module.destinations.repository.DestinationFollowRepository;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.destinations.service.command.DestinationFollowCommandService;
import com.wedservice.backend.module.destinations.service.query.DestinationFollowQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationFollowService implements DestinationFollowCommandService, DestinationFollowQueryService {

    private final DestinationFollowRepository followRepository;
    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public DestinationFollowResponse followDestination(UUID destinationUuid, FollowDestinationRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        Destination destination = destinationRepository.findByUuid(destinationUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + destinationUuid));

        if (followRepository.existsByUserIdAndDestinationId(userId, destination.getId())) {
            throw new BadRequestException("Already following this destination");
        }

        DestinationFollow follow = DestinationFollow.builder()
                .userId(userId)
                .destination(destination)
                .notifyEvent(request.getNotifyEvent() != null ? request.getNotifyEvent() : true)
                .notifyVoucher(request.getNotifyVoucher() != null ? request.getNotifyVoucher() : true)
                .notifyNewTour(request.getNotifyNewTour() != null ? request.getNotifyNewTour() : true)
                .notifyBestSeason(request.getNotifyBestSeason() != null ? request.getNotifyBestSeason() : true)
                .build();

        return destinationMapper.toFollowResponse(followRepository.save(follow));
    }

    @Transactional
    public void unfollowDestination(UUID destinationUuid) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        Destination destination = destinationRepository.findByUuid(destinationUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + destinationUuid));

        if (!followRepository.existsByUserIdAndDestinationId(userId, destination.getId())) {
            throw new ResourceNotFoundException("Not following this destination");
        }
        followRepository.deleteByUserIdAndDestinationId(userId, destination.getId());
    }

    @Transactional
    public DestinationFollowResponse updateFollowSettings(UUID destinationUuid, FollowDestinationRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        Destination destination = destinationRepository.findByUuid(destinationUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found: " + destinationUuid));

        DestinationFollow follow = followRepository.findByUserIdAndDestinationId(userId, destination.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Not following this destination"));

        if (request.getNotifyEvent() != null) follow.setNotifyEvent(request.getNotifyEvent());
        if (request.getNotifyVoucher() != null) follow.setNotifyVoucher(request.getNotifyVoucher());
        if (request.getNotifyNewTour() != null) follow.setNotifyNewTour(request.getNotifyNewTour());
        if (request.getNotifyBestSeason() != null) follow.setNotifyBestSeason(request.getNotifyBestSeason());

        return destinationMapper.toFollowResponse(followRepository.save(follow));
    }

    @Transactional(readOnly = true)
    public PageResponse<DestinationFollowResponse> getMyFollows(int page, int size) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DestinationFollow> followPage = followRepository.findByUserId(userId, pageable);

        return PageResponse.of(followPage.map(destinationMapper::toFollowResponse));
    }
}
