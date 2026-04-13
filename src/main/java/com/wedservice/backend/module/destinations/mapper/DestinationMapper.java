package com.wedservice.backend.module.destinations.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.wedservice.backend.common.mapper.BaseMapper;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.common.util.SlugUtils;
import com.wedservice.backend.module.destinations.dto.request.DestinationRequest;
import com.wedservice.backend.module.destinations.dto.request.*;
import com.wedservice.backend.module.destinations.dto.response.*;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DataNormalizer.class, SlugUtils.class, CrowdLevel.class, DestinationStatus.class})
public interface DestinationMapper extends BaseMapper<DestinationResponse, Destination> {

    @Override
    @Mapping(target = "uuid", source = "uuid")
    DestinationResponse toDto(Destination entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "name", expression = "java(DataNormalizer.normalize(request.getName()))")
    @Mapping(target = "slug", expression = "java(resolveSlug(request))")
    @Mapping(target = "code", expression = "java(DataNormalizer.normalize(request.getCode()))")
    @Mapping(target = "countryCode", expression = "java(request.getCountryCode() != null ? request.getCountryCode().toUpperCase() : \"VN\")")
    @Mapping(target = "province", expression = "java(DataNormalizer.normalize(request.getProvince()))")
    @Mapping(target = "district", expression = "java(DataNormalizer.normalize(request.getDistrict()))")
    @Mapping(target = "region", expression = "java(DataNormalizer.normalize(request.getRegion()))")
    @Mapping(target = "address", expression = "java(DataNormalizer.normalize(request.getAddress()))")
    @Mapping(target = "shortDescription", expression = "java(DataNormalizer.normalize(request.getShortDescription()))")
    @Mapping(target = "description", expression = "java(DataNormalizer.normalize(request.getDescription()))")
    @Mapping(target = "crowdLevelDefault", expression = "java(request.getCrowdLevelDefault() != null ? request.getCrowdLevelDefault() : CrowdLevel.MEDIUM)")
    @Mapping(target = "isActive", expression = "java(request.getIsActive() != null ? request.getIsActive() : true)")
    @Mapping(target = "status", expression = "java(DestinationStatus.APPROVED)")
    @Mapping(target = "mediaList", ignore = true)
    @Mapping(target = "foods", ignore = true)
    @Mapping(target = "specialties", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "tips", ignore = true)
    @Mapping(target = "events", ignore = true)
    Destination toEntity(DestinationRequest request);

    @Mapping(target = "name", expression = "java(DataNormalizer.normalize(request.getName()))")
    @Mapping(target = "slug", source = "normalizedSlug")
    @Mapping(target = "code", source = "normalizedCode")
    @Mapping(target = "countryCode", expression = "java(request.getCountryCode() != null ? request.getCountryCode().toUpperCase() : destination.getCountryCode())")
    @Mapping(target = "province", expression = "java(DataNormalizer.normalize(request.getProvince()))")
    @Mapping(target = "district", expression = "java(DataNormalizer.normalize(request.getDistrict()))")
    @Mapping(target = "region", expression = "java(DataNormalizer.normalize(request.getRegion()))")
    @Mapping(target = "address", expression = "java(DataNormalizer.normalize(request.getAddress()))")
    @Mapping(target = "shortDescription", expression = "java(DataNormalizer.normalize(request.getShortDescription()))")
    @Mapping(target = "description", expression = "java(DataNormalizer.normalize(request.getDescription()))")
    @Mapping(target = "bestTimeFromMonth", source = "request.bestTimeFromMonth")
    @Mapping(target = "bestTimeToMonth", source = "request.bestTimeToMonth")
    @Mapping(target = "crowdLevelDefault", expression = "java(defaultCrowdLevel(request.getCrowdLevelDefault(), destination.getCrowdLevelDefault()))")
    @Mapping(target = "isFeatured", expression = "java(defaultBoolean(request.getIsFeatured(), destination.getIsFeatured()))")
    @Mapping(target = "isActive", expression = "java(defaultBoolean(request.getIsActive(), destination.getIsActive()))")
    @Mapping(target = "isOfficial", expression = "java(defaultBoolean(request.getIsOfficial(), destination.getIsOfficial()))")
    @Mapping(target = "mediaList", ignore = true)
    @Mapping(target = "foods", ignore = true)
    @Mapping(target = "specialties", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "tips", ignore = true)
    @Mapping(target = "events", ignore = true)
    void applyAdminUpdate(
            @MappingTarget Destination destination,
            DestinationRequest request,
            String normalizedCode,
            String normalizedSlug
    );

    @Mapping(target = "uuid", source = "uuid")
    DestinationDetailResponse toDetailResponse(Destination destination);

    @Mapping(target = "id", ignore = true)
    DestinationMedia toMediaEntity(DestinationMediaRequest request);

    DestinationMediaResponse toMediaResponse(DestinationMedia entity);

    @Mapping(target = "id", ignore = true)
    DestinationFood toFoodEntity(DestinationFoodRequest request);

    DestinationFoodResponse toFoodResponse(DestinationFood entity);

    @Mapping(target = "id", ignore = true)
    DestinationEvent toEventEntity(DestinationEventRequest request);

    DestinationEventResponse toEventResponse(DestinationEvent entity);

    @Mapping(target = "destinationUuid", source = "destination.uuid")
    @Mapping(target = "destinationName", source = "destination.name")
    DestinationFollowResponse toFollowResponse(DestinationFollow follow);

    default String resolveSlug(DestinationRequest request) {
        String name = DataNormalizer.normalize(request.getName());
        String slug = DataNormalizer.normalize(request.getSlug());
        if (!org.springframework.util.StringUtils.hasText(slug) && org.springframework.util.StringUtils.hasText(name)) {
            return SlugUtils.toSlug(name);
        }
        return slug;
    }

    default CrowdLevel defaultCrowdLevel(CrowdLevel requestValue, CrowdLevel existingValue) {
        return requestValue != null ? requestValue : existingValue;
    }

    default Boolean defaultBoolean(Boolean requestValue, Boolean existingValue) {
        return requestValue != null ? requestValue : existingValue;
    }

    @AfterMapping
    default void mapSubEntities(@MappingTarget Destination destination, DestinationRequest request) {
        if (request == null) return;

        // Media
        if (request.getMediaList() != null) {
            destination.getMediaList().clear();
            request.getMediaList().forEach(r -> {
                DestinationMedia entity = DestinationMedia.builder()
                        .destination(destination)
                        .mediaType(r.getMediaType() != null ? r.getMediaType() : MediaType.IMAGE)
                        .mediaUrl(DataNormalizer.normalize(r.getMediaUrl()))
                        .altText(DataNormalizer.normalize(r.getAltText()))
                        .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0)
                        .isActive(r.getIsActive() != null ? r.getIsActive() : true)
                        .build();
                destination.getMediaList().add(entity);
            });
        }

        // Foods
        if (request.getFoods() != null) {
            destination.getFoods().clear();
            request.getFoods().forEach(r -> {
                DestinationFood entity = DestinationFood.builder()
                        .destination(destination)
                        .foodName(DataNormalizer.normalize(r.getFoodName()))
                        .description(DataNormalizer.normalize(r.getDescription()))
                        .isFeatured(r.getIsFeatured() != null ? r.getIsFeatured() : true)
                        .build();
                destination.getFoods().add(entity);
            });
        }

        // Specialties
        if (request.getSpecialties() != null) {
            destination.getSpecialties().clear();
            request.getSpecialties().forEach(r -> {
                DestinationSpecialty entity = DestinationSpecialty.builder()
                        .destination(destination)
                        .specialtyName(DataNormalizer.normalize(r.getSpecialtyName()))
                        .description(DataNormalizer.normalize(r.getDescription()))
                        .build();
                destination.getSpecialties().add(entity);
            });
        }

        // Activities
        if (request.getActivities() != null) {
            destination.getActivities().clear();
            request.getActivities().forEach(r -> {
                DestinationActivity entity = DestinationActivity.builder()
                        .destination(destination)
                        .activityName(DataNormalizer.normalize(r.getActivityName()))
                        .description(DataNormalizer.normalize(r.getDescription()))
                        .activityScore(r.getActivityScore() != null ? r.getActivityScore() : BigDecimal.ZERO)
                        .build();
                destination.getActivities().add(entity);
            });
        }

        // Tips
        if (request.getTips() != null) {
            destination.getTips().clear();
            request.getTips().forEach(r -> {
                DestinationTip entity = DestinationTip.builder()
                        .destination(destination)
                        .tipTitle(DataNormalizer.normalize(r.getTipTitle()))
                        .tipContent(DataNormalizer.normalize(r.getTipContent()))
                        .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0)
                        .build();
                destination.getTips().add(entity);
            });
        }

        // Events
        if (request.getEvents() != null) {
            destination.getEvents().clear();
            request.getEvents().forEach(r -> {
                DestinationEvent entity = DestinationEvent.builder()
                        .destination(destination)
                        .eventName(DataNormalizer.normalize(r.getEventName()))
                        .eventType(DataNormalizer.normalize(r.getEventType()))
                        .description(DataNormalizer.normalize(r.getDescription()))
                        .startsAt(r.getStartsAt())
                        .endsAt(r.getEndsAt())
                        .notifyAllFollowers(r.getNotifyAllFollowers() != null ? r.getNotifyAllFollowers() : false)
                        .isActive(r.getIsActive() != null ? r.getIsActive() : true)
                        .build();
                destination.getEvents().add(entity);
            });
        }
    }

    default List<DestinationMediaResponse> mapMediaList(List<DestinationMedia> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(m -> DestinationMediaResponse.builder()
                .mediaType(m.getMediaType())
                .mediaUrl(m.getMediaUrl())
                .altText(m.getAltText())
                .sortOrder(m.getSortOrder())
                .isActive(m.getIsActive())
                .build()).toList();
    }

    default List<DestinationFoodResponse> mapFoodList(List<DestinationFood> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(f -> DestinationFoodResponse.builder()
                .foodName(f.getFoodName())
                .description(f.getDescription())
                .isFeatured(f.getIsFeatured())
                .build()).toList();
    }

    default List<DestinationSpecialtyResponse> mapSpecialtyList(List<DestinationSpecialty> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(s -> DestinationSpecialtyResponse.builder()
                .specialtyName(s.getSpecialtyName())
                .description(s.getDescription())
                .build()).toList();
    }

    default List<DestinationActivityResponse> mapActivityList(List<DestinationActivity> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(a -> DestinationActivityResponse.builder()
                .activityName(a.getActivityName())
                .description(a.getDescription())
                .activityScore(a.getActivityScore())
                .build()).toList();
    }

    default List<DestinationTipResponse> mapTipList(List<DestinationTip> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(t -> DestinationTipResponse.builder()
                .tipTitle(t.getTipTitle())
                .tipContent(t.getTipContent())
                .sortOrder(t.getSortOrder())
                .build()).toList();
    }

    default List<DestinationEventResponse> mapEventList(List<DestinationEvent> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(e -> DestinationEventResponse.builder()
                .eventName(e.getEventName())
                .eventType(e.getEventType())
                .description(e.getDescription())
                .startsAt(e.getStartsAt())
                .endsAt(e.getEndsAt())
                .notifyAllFollowers(e.getNotifyAllFollowers())
                .isActive(e.getIsActive())
                .build()).toList();
    }
}
