package com.wedservice.backend.module.destinations.mapper;

import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DestinationMapper {

    public DestinationResponse toResponse(Destination destination) {
        if (destination == null) {
            return null;
        }

        return DestinationResponse.builder()
                .uuid(destination.getUuid())
                .code(destination.getCode())
                .name(destination.getName())
                .slug(destination.getSlug())
                .countryCode(destination.getCountryCode())
                .province(destination.getProvince())
                .district(destination.getDistrict())
                .region(destination.getRegion())
                .address(destination.getAddress())
                .latitude(destination.getLatitude())
                .longitude(destination.getLongitude())
                .shortDescription(destination.getShortDescription())
                .description(destination.getDescription())
                .bestTimeFromMonth(destination.getBestTimeFromMonth())
                .bestTimeToMonth(destination.getBestTimeToMonth())
                .crowdLevelDefault(destination.getCrowdLevelDefault())
                .isFeatured(destination.getIsFeatured())
                .isActive(destination.getIsActive())
                .status(destination.getStatus())
                .proposedBy(destination.getProposedBy())
                .verifiedBy(destination.getVerifiedBy())
                .rejectionReason(destination.getRejectionReason())
                .isOfficial(destination.getIsOfficial())
                .createdAt(destination.getCreatedAt())
                .updatedAt(destination.getUpdatedAt())
                .build();
    }

    public Destination toEntity(DestinationRequest request) {
        if (request == null) {
            return null;
        }

        return Destination.builder()
                .code(normalize(request.getCode()))
                .name(normalize(request.getName()))
                .slug(normalize(request.getSlug()))
                .countryCode(StringUtils.hasText(request.getCountryCode()) ? request.getCountryCode().toUpperCase() : "VN")
                .province(normalize(request.getProvince()))
                .district(normalize(request.getDistrict()))
                .region(normalize(request.getRegion()))
                .address(normalize(request.getAddress()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .shortDescription(normalize(request.getShortDescription()))
                .description(normalize(request.getDescription()))
                .bestTimeFromMonth(request.getBestTimeFromMonth())
                .bestTimeToMonth(request.getBestTimeToMonth())
                .crowdLevelDefault(request.getCrowdLevelDefault() != null ? request.getCrowdLevelDefault() : com.wedservice.backend.module.destinations.entity.CrowdLevel.MEDIUM)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .status(DestinationStatus.APPROVED) // Default for direct admin create
                .isOfficial(true) // Default for admin create
                .build();
    }

    public void updateEntity(Destination destination, DestinationRequest request) {
        if (request == null) {
            return;
        }

        destination.setCode(normalize(request.getCode()));
        destination.setName(normalize(request.getName()));
        destination.setSlug(normalize(request.getSlug()));
        if (StringUtils.hasText(request.getCountryCode())) {
            destination.setCountryCode(request.getCountryCode().toUpperCase());
        }
        destination.setProvince(normalize(request.getProvince()));
        destination.setDistrict(normalize(request.getDistrict()));
        destination.setRegion(normalize(request.getRegion()));
        destination.setAddress(normalize(request.getAddress()));
        destination.setLatitude(request.getLatitude());
        destination.setLongitude(request.getLongitude());
        destination.setShortDescription(normalize(request.getShortDescription()));
        destination.setDescription(normalize(request.getDescription()));
        destination.setBestTimeFromMonth(request.getBestTimeFromMonth());
        destination.setBestTimeToMonth(request.getBestTimeToMonth());
        if (request.getCrowdLevelDefault() != null) {
            destination.setCrowdLevelDefault(request.getCrowdLevelDefault());
        }
        if (request.getIsFeatured() != null) {
            destination.setIsFeatured(request.getIsFeatured());
        }
        if (request.getIsActive() != null) {
            destination.setIsActive(request.getIsActive());
        }
        if (request.getIsOfficial() != null) {
            destination.setIsOfficial(request.getIsOfficial());
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
