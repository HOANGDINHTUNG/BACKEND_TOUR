package com.wedservice.backend.module.destinations.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.ProposeDestinationRequest;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.common.util.DataNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DestinationValidator {

    private final DestinationRepository destinationRepository;

    public void validatePropose(DestinationRequest request) {
        if (request == null) return;
        if (request.getCode() != null && destinationRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BadRequestException("Destination code already exists: " + request.getCode());
        }
        if (request.getSlug() != null && destinationRepository.existsBySlugIgnoreCase(request.getSlug())) {
            throw new BadRequestException("Destination slug already exists: " + request.getSlug());
        }
    }

    public void validatePropose(ProposeDestinationRequest request) {
        if (request == null) {
            return;
        }

        String normalizedName = DataNormalizer.normalize(request.getName());
        String normalizedProvince = DataNormalizer.normalize(request.getProvince());

        if (!StringUtils.hasText(normalizedName) || !StringUtils.hasText(normalizedProvince)) {
            return;
        }

        boolean alreadySubmitted = destinationRepository.existsByNameIgnoreCaseAndProvinceIgnoreCaseAndStatusIn(
                normalizedName,
                normalizedProvince,
                List.of(DestinationStatus.PENDING, DestinationStatus.APPROVED)
        );

        if (alreadySubmitted) {
            throw new BadRequestException("Destination has already been submitted or approved");
        }
    }
}
