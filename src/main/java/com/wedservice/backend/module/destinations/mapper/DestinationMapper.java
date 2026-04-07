package com.wedservice.backend.module.destinations.mapper;

import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.response.DestinationActivityResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationEventResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationFollowResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationFoodResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationMediaResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationSpecialtyResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationTipResponse;
import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationActivity;
import com.wedservice.backend.module.destinations.entity.DestinationEvent;
import com.wedservice.backend.module.destinations.entity.DestinationFollow;
import com.wedservice.backend.module.destinations.entity.DestinationFood;
import com.wedservice.backend.module.destinations.entity.DestinationMedia;
import com.wedservice.backend.module.destinations.entity.DestinationSpecialty;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.entity.DestinationTip;
import com.wedservice.backend.module.destinations.entity.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class DestinationMapper {

    // === Destination summary (for list/search) ===

    public DestinationResponse toResponse(Destination destination) {
        if (destination == null) return null;

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

    // === Destination detail (includes sub-entities) ===

    public DestinationDetailResponse toDetailResponse(Destination destination) {
        if (destination == null) return null;

        return DestinationDetailResponse.builder()
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
                .mediaList(toMediaResponseList(destination.getMediaList()))
                .foods(toFoodResponseList(destination.getFoods()))
                .specialties(toSpecialtyResponseList(destination.getSpecialties()))
                .activities(toActivityResponseList(destination.getActivities()))
                .tips(toTipResponseList(destination.getTips()))
                .events(toEventResponseList(destination.getEvents()))
                .build();
    }

    // === Create entity from request ===

    public Destination toEntity(DestinationRequest request) {
        if (request == null) return null;

        String name = normalize(request.getName());
        String slug = normalize(request.getSlug());
        if (!StringUtils.hasText(slug) && StringUtils.hasText(name)) {
            slug = com.wedservice.backend.common.util.SlugUtils.toSlug(name);
        }

        Destination destination = Destination.builder()
                .code(normalize(request.getCode()))
                .name(name)
                .slug(slug)
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
                .crowdLevelDefault(request.getCrowdLevelDefault() != null ? request.getCrowdLevelDefault() : CrowdLevel.MEDIUM)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .status(DestinationStatus.APPROVED)
                .isOfficial(true)
                .build();

        applySubEntities(destination, request);
        return destination;
    }

    // === Update entity from request ===

    public void updateEntity(Destination destination, DestinationRequest request) {
        if (request == null) return;

        String name = normalize(request.getName());
        String slug = normalize(request.getSlug());
        if (!StringUtils.hasText(slug) && StringUtils.hasText(name)) {
            slug = com.wedservice.backend.common.util.SlugUtils.toSlug(name);
        }

        destination.setCode(normalize(request.getCode()));
        destination.setName(name);
        destination.setSlug(slug);
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

        applySubEntities(destination, request);
    }

    // === Follow mapping ===

    public DestinationFollowResponse toFollowResponse(DestinationFollow follow) {
        if (follow == null) return null;

        Destination dest = follow.getDestination();
        return DestinationFollowResponse.builder()
                .id(follow.getId())
                .destinationUuid(dest != null ? dest.getUuid() : null)
                .destinationName(dest != null ? dest.getName() : null)
                .notifyEvent(follow.getNotifyEvent())
                .notifyVoucher(follow.getNotifyVoucher())
                .notifyNewTour(follow.getNotifyNewTour())
                .notifyBestSeason(follow.getNotifyBestSeason())
                .createdAt(follow.getCreatedAt())
                .build();
    }

    // === Sub-entity list mapping helpers ===

    private void applySubEntities(Destination destination, DestinationRequest request) {
        // Media
        destination.getMediaList().clear();
        if (request.getMediaList() != null) {
            request.getMediaList().forEach(r -> {
                DestinationMedia entity = DestinationMedia.builder()
                        .destination(destination)
                        .mediaType(r.getMediaType() != null ? r.getMediaType() : MediaType.IMAGE)
                        .mediaUrl(normalize(r.getMediaUrl()))
                        .altText(normalize(r.getAltText()))
                        .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0)
                        .isActive(r.getIsActive() != null ? r.getIsActive() : true)
                        .build();
                destination.getMediaList().add(entity);
            });
        }

        // Foods
        destination.getFoods().clear();
        if (request.getFoods() != null) {
            request.getFoods().forEach(r -> {
                DestinationFood entity = DestinationFood.builder()
                        .destination(destination)
                        .foodName(normalize(r.getFoodName()))
                        .description(normalize(r.getDescription()))
                        .isFeatured(r.getIsFeatured() != null ? r.getIsFeatured() : true)
                        .build();
                destination.getFoods().add(entity);
            });
        }

        // Specialties
        destination.getSpecialties().clear();
        if (request.getSpecialties() != null) {
            request.getSpecialties().forEach(r -> {
                DestinationSpecialty entity = DestinationSpecialty.builder()
                        .destination(destination)
                        .specialtyName(normalize(r.getSpecialtyName()))
                        .description(normalize(r.getDescription()))
                        .build();
                destination.getSpecialties().add(entity);
            });
        }

        // Activities
        destination.getActivities().clear();
        if (request.getActivities() != null) {
            request.getActivities().forEach(r -> {
                DestinationActivity entity = DestinationActivity.builder()
                        .destination(destination)
                        .activityName(normalize(r.getActivityName()))
                        .description(normalize(r.getDescription()))
                        .activityScore(r.getActivityScore() != null ? r.getActivityScore() : BigDecimal.ZERO)
                        .build();
                destination.getActivities().add(entity);
            });
        }

        // Tips
        destination.getTips().clear();
        if (request.getTips() != null) {
            request.getTips().forEach(r -> {
                DestinationTip entity = DestinationTip.builder()
                        .destination(destination)
                        .tipTitle(normalize(r.getTipTitle()))
                        .tipContent(normalize(r.getTipContent()))
                        .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0)
                        .build();
                destination.getTips().add(entity);
            });
        }

        // Events
        destination.getEvents().clear();
        if (request.getEvents() != null) {
            request.getEvents().forEach(r -> {
                DestinationEvent entity = DestinationEvent.builder()
                        .destination(destination)
                        .eventName(normalize(r.getEventName()))
                        .eventType(normalize(r.getEventType()))
                        .description(normalize(r.getDescription()))
                        .startsAt(r.getStartsAt())
                        .endsAt(r.getEndsAt())
                        .notifyAllFollowers(r.getNotifyAllFollowers() != null ? r.getNotifyAllFollowers() : false)
                        .isActive(r.getIsActive() != null ? r.getIsActive() : true)
                        .build();
                destination.getEvents().add(entity);
            });
        }
    }

    // === Sub-entity response list mappers ===

    private List<DestinationMediaResponse> toMediaResponseList(List<DestinationMedia> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(m -> DestinationMediaResponse.builder()
                .id(m.getId())
                .mediaType(m.getMediaType())
                .mediaUrl(m.getMediaUrl())
                .altText(m.getAltText())
                .sortOrder(m.getSortOrder())
                .isActive(m.getIsActive())
                .build()).toList();
    }

    private List<DestinationFoodResponse> toFoodResponseList(List<DestinationFood> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(f -> DestinationFoodResponse.builder()
                .id(f.getId())
                .foodName(f.getFoodName())
                .description(f.getDescription())
                .isFeatured(f.getIsFeatured())
                .build()).toList();
    }

    private List<DestinationSpecialtyResponse> toSpecialtyResponseList(List<DestinationSpecialty> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(s -> DestinationSpecialtyResponse.builder()
                .id(s.getId())
                .specialtyName(s.getSpecialtyName())
                .description(s.getDescription())
                .build()).toList();
    }

    private List<DestinationActivityResponse> toActivityResponseList(List<DestinationActivity> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(a -> DestinationActivityResponse.builder()
                .id(a.getId())
                .activityName(a.getActivityName())
                .description(a.getDescription())
                .activityScore(a.getActivityScore())
                .build()).toList();
    }

    private List<DestinationTipResponse> toTipResponseList(List<DestinationTip> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(t -> DestinationTipResponse.builder()
                .id(t.getId())
                .tipTitle(t.getTipTitle())
                .tipContent(t.getTipContent())
                .sortOrder(t.getSortOrder())
                .build()).toList();
    }

    private List<DestinationEventResponse> toEventResponseList(List<DestinationEvent> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(e -> DestinationEventResponse.builder()
                .id(e.getId())
                .eventName(e.getEventName())
                .eventType(e.getEventType())
                .description(e.getDescription())
                .startsAt(e.getStartsAt())
                .endsAt(e.getEndsAt())
                .notifyAllFollowers(e.getNotifyAllFollowers())
                .isActive(e.getIsActive())
                .build()).toList();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
