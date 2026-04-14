package com.wedservice.backend.module.destinations.service.query.impl;

import com.querydsl.core.BooleanBuilder;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationMedia;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.entity.QDestination;
import com.wedservice.backend.module.destinations.mapper.DestinationMapper;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.destinations.dto.request.DestinationSearchRequest;
import org.springframework.cache.annotation.Cacheable;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicDetailResponse;
import com.wedservice.backend.module.destinations.dto.response.DestinationPublicResponse;
import com.wedservice.backend.module.destinations.service.query.DestinationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationQueryServiceImpl implements DestinationQueryService {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;

    @Override
    @Cacheable(value = "destinations", key = "#request")
    @Transactional(readOnly = true)
    public PageResponse<DestinationPublicResponse> searchApprovedDestinations(DestinationSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        QDestination qDestination = QDestination.destination;
        BooleanBuilder builder = new BooleanBuilder();

        String keyword = DataNormalizer.normalize(request.getKeyword());
        if (StringUtils.hasText(keyword)) {
            builder.and(qDestination.name.containsIgnoreCase(keyword)
                    .or(qDestination.code.containsIgnoreCase(keyword)));
        }

        if (StringUtils.hasText(request.getProvince())) {
            builder.and(qDestination.province.eq(request.getProvince()));
        }

        if (StringUtils.hasText(request.getRegion())) {
            builder.and(qDestination.region.eq(request.getRegion()));
        }

        if (request.getCrowdLevel() != null) {
            builder.and(qDestination.crowdLevelDefault.eq(request.getCrowdLevel()));
        }

        if (request.getIsFeatured() != null) {
            builder.and(qDestination.isFeatured.eq(request.getIsFeatured()));
        }

        builder.and(qDestination.status.eq(DestinationStatus.APPROVED));
        builder.and(qDestination.deletedAt.isNull());
        builder.and(qDestination.isOfficial.isTrue());

        Page<Destination> page = destinationRepository.findAll(builder, pageable);
        return PageResponse.of(page.map(this::toPublicResponse));
    }

    @Override
    @Cacheable(value = "destination-details", key = "#uuid")
    @Transactional(readOnly = true)
    public DestinationPublicDetailResponse getApprovedDestinationByUuid(UUID uuid) {
        Destination destination = destinationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with uuid: " + uuid));

        if (destination.getStatus() != DestinationStatus.APPROVED || destination.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Destination not found or not approved");
        }

        return toPublicDetailResponse(destination);
    }

    private DestinationPublicResponse toPublicResponse(Destination destination) {
        return DestinationPublicResponse.builder()
                .uuid(destination.getUuid())
                .name(destination.getName())
                .slug(destination.getSlug())
                .countryCode(destination.getCountryCode())
                .province(destination.getProvince())
                .district(destination.getDistrict())
                .region(destination.getRegion())
                .shortDescription(destination.getShortDescription())
                .bestTimeFromMonth(destination.getBestTimeFromMonth())
                .bestTimeToMonth(destination.getBestTimeToMonth())
                .crowdLevelDefault(destination.getCrowdLevelDefault())
                .isFeatured(destination.getIsFeatured())
                .coverImageUrl(resolveCoverImage(destination))
                .build();
    }

    private DestinationPublicDetailResponse toPublicDetailResponse(Destination destination) {
        return DestinationPublicDetailResponse.builder()
                .uuid(destination.getUuid())
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
                .mediaList(destinationMapper.mapMediaList(destination.getMediaList()))
                .foods(destinationMapper.mapFoodList(destination.getFoods()))
                .specialties(destinationMapper.mapSpecialtyList(destination.getSpecialties()))
                .activities(destinationMapper.mapActivityList(destination.getActivities()))
                .tips(destinationMapper.mapTipList(destination.getTips()))
                .events(destinationMapper.mapEventList(destination.getEvents()))
                .build();
    }

    private String resolveCoverImage(Destination destination) {
        return destination.getMediaList().stream()
                .filter(media -> Boolean.TRUE.equals(media.getIsActive()))
                .sorted(Comparator.comparing(DestinationMedia::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(DestinationMedia::getMediaUrl)
                .findFirst()
                .orElse(null);
    }
}
