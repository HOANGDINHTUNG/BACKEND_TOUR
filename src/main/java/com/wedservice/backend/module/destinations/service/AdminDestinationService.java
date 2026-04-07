package com.wedservice.backend.module.destinations.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import com.wedservice.backend.module.destinations.dto.request.RejectProposalRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.mapper.DestinationMapper;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;

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
public class AdminDestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public PageResponse<DestinationResponse> searchDestinations(DestinationSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Destination> page = destinationRepository.searchDestinations(
                request.getKeyword(),
                request.getProvince(),
                request.getRegion(),
                request.getCrowdLevel(),
                request.getIsFeatured(),
                request.getIsActive(),
                request.getStatus(),
                request.getIsOfficial(),
                pageable
        );

        return PageResponse.of(page.map(destinationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public DestinationDetailResponse getDestinationByUuid(UUID uuid) {
        Destination destination = destinationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + uuid));
        return destinationMapper.toDetailResponse(destination);
    }

    @Transactional
    public DestinationDetailResponse createDestination(DestinationRequest request) {
        if (destinationRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BadRequestException("Destination code already exists: " + request.getCode());
        }
        if (destinationRepository.existsBySlugIgnoreCase(request.getSlug())) {
            throw new BadRequestException("Destination slug already exists: " + request.getSlug());
        }

        Destination destination = destinationMapper.toEntity(request);
        return destinationMapper.toDetailResponse(destinationRepository.save(destination));
    }

    @Transactional
    public DestinationDetailResponse updateDestination(UUID uuid, DestinationRequest request) {
        Destination destination = destinationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + uuid));

        if (destinationRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode(), destination.getId())) {
            throw new BadRequestException("Destination code already exists: " + request.getCode());
        }
        if (destinationRepository.existsBySlugIgnoreCaseAndIdNot(request.getSlug(), destination.getId())) {
            throw new BadRequestException("Destination slug already exists: " + request.getSlug());
        }

        destinationMapper.updateEntity(destination, request);
        return destinationMapper.toDetailResponse(destinationRepository.save(destination));
    }

    @Transactional
    public void deleteDestination(UUID uuid) {
        Destination destination = destinationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + uuid));
        destination.setIsActive(false);
        destinationRepository.save(destination);
    }

    @Transactional
    public DestinationDetailResponse approveProposal(UUID uuid) {
        Destination destination = destinationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + uuid));

        if (destination.getStatus() != DestinationStatus.PENDING) {
            throw new BadRequestException("Only pending proposals can be approved");
        }

        destination.setStatus(DestinationStatus.APPROVED);
        destination.setIsOfficial(true);
        destination.setVerifiedBy(authenticatedUserProvider.getRequiredCurrentUserId());
        return destinationMapper.toDetailResponse(destinationRepository.save(destination));
    }

    @Transactional
    public DestinationDetailResponse rejectProposal(UUID uuid, RejectProposalRequest request) {
        Destination destination = destinationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + uuid));

        if (destination.getStatus() != DestinationStatus.PENDING) {
            throw new BadRequestException("Only pending proposals can be rejected");
        }

        destination.setStatus(DestinationStatus.REJECTED);
        destination.setRejectionReason(request.getReason());
        destination.setVerifiedBy(authenticatedUserProvider.getRequiredCurrentUserId());
        return destinationMapper.toDetailResponse(destinationRepository.save(destination));
    }
}
