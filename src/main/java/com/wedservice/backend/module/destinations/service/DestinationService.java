package com.wedservice.backend.module.destinations.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.mapper.DestinationMapper;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
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
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public PageResponse<DestinationResponse> searchApprovedDestinations(DestinationSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Destination> page = destinationRepository.searchDestinations(
                request.getKeyword(),
                request.getProvince(),
                request.getRegion(),
                request.getCrowdLevel(),
                request.getIsFeatured(),
                request.getIsActive() != null ? request.getIsActive() : true,
                DestinationStatus.APPROVED,
                true,
                pageable
        );

        return PageResponse.of(page.map(destinationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public DestinationDetailResponse getApprovedDestinationByUuid(UUID uuid) {
        Destination destination = destinationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + uuid));

        if (destination.getStatus() != DestinationStatus.APPROVED || !destination.getIsActive()) {
            throw new ResourceNotFoundException("Destination not found or not approved");
        }

        return destinationMapper.toDetailResponse(destination);
    }

    @Transactional
    public DestinationDetailResponse proposeDestination(DestinationRequest request) {
        if (destinationRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BadRequestException("Destination code already exists: " + request.getCode());
        }
        if (destinationRepository.existsBySlugIgnoreCase(request.getSlug())) {
            throw new BadRequestException("Destination slug already exists: " + request.getSlug());
        }

        Destination destination = destinationMapper.toEntity(request);
        destination.setStatus(DestinationStatus.PENDING);
        destination.setIsOfficial(false);
        destination.setProposedBy(authenticatedUserProvider.getRequiredCurrentUserId());

        return destinationMapper.toDetailResponse(destinationRepository.save(destination));
    }
}
