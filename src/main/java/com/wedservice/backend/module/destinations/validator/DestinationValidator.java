package com.wedservice.backend.module.destinations.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
