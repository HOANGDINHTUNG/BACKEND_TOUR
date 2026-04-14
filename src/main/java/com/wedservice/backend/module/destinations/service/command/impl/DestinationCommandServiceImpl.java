package com.wedservice.backend.module.destinations.service.command.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.common.util.SlugUtils;
import com.wedservice.backend.module.destinations.dto.request.ProposeDestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationProposalResponse;
import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.destinations.service.command.DestinationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class DestinationCommandServiceImpl implements DestinationCommandService {

    private final DestinationRepository destinationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public DestinationProposalResponse proposeDestination(ProposeDestinationRequest request) {
        String normalizedName = DataNormalizer.normalize(request.getName());
        String normalizedProvince = DataNormalizer.normalize(request.getProvince());
        String normalizedDistrict = DataNormalizer.normalize(request.getDistrict());
        String normalizedRegion = DataNormalizer.normalize(request.getRegion());
        String normalizedSlug = generateUniqueSlug(normalizedName);
        String generatedCode = generateSuggestionCode();

        log.info("Proposing new destination: code={}, name={}", generatedCode, normalizedName);
        try {
            Destination destination = Destination.builder()
                    .code(generatedCode)
                    .name(normalizedName)
                    .slug(normalizedSlug)
                    .countryCode(StringUtils.hasText(request.getCountryCode()) ? request.getCountryCode().toUpperCase() : "VN")
                    .province(normalizedProvince)
                    .district(normalizedDistrict)
                    .region(normalizedRegion)
                    .address(DataNormalizer.normalize(request.getAddress()))
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .shortDescription(DataNormalizer.normalize(request.getShortDescription()))
                    .description(DataNormalizer.normalize(request.getDescription()))
                    .bestTimeFromMonth(request.getBestTimeFromMonth())
                    .bestTimeToMonth(request.getBestTimeToMonth())
                    .crowdLevelDefault(request.getCrowdLevelDefault() == null ? CrowdLevel.MEDIUM : request.getCrowdLevelDefault())
                    .isFeatured(false)
                    .isActive(true)
                    .status(DestinationStatus.PENDING)
                    .isOfficial(false)
                    .proposedBy(authenticatedUserProvider.getRequiredCurrentUserId())
                    .build();

            Destination saved = destinationRepository.save(destination);
            log.info("Destination saved successfully: uuid={}", saved.getUuid());
            return DestinationProposalResponse.builder()
                    .uuid(saved.getUuid())
                    .name(saved.getName())
                    .province(saved.getProvince())
                    .district(saved.getDistrict())
                    .region(saved.getRegion())
                    .status(saved.getStatus())
                    .submittedAt(saved.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("CRITICAL: Failed to propose destination. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String generateSuggestionCode() {
        String code;
        do {
            code = "SUG-" + System.currentTimeMillis();
        } while (destinationRepository.existsByCodeIgnoreCase(code));
        return code;
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtils.toSlug(name);
        String candidate = baseSlug;
        int suffix = 1;
        while (destinationRepository.existsBySlugIgnoreCase(candidate)) {
            suffix++;
            candidate = baseSlug + "-" + suffix;
        }
        return candidate;
    }
}
