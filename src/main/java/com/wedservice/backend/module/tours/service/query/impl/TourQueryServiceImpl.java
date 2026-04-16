package com.wedservice.backend.module.tours.service.query.impl;

import com.querydsl.core.BooleanBuilder;
import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.CancellationPolicyResponse;
import com.wedservice.backend.module.tours.dto.response.CancellationPolicyRuleResponse;
import com.wedservice.backend.module.tours.dto.response.ItineraryItemResponse;
import com.wedservice.backend.module.tours.dto.response.TagResponse;
import com.wedservice.backend.module.tours.dto.response.TourChecklistItemResponse;
import com.wedservice.backend.module.tours.dto.response.TourItineraryDayResponse;
import com.wedservice.backend.module.tours.dto.response.TourMediaResponse;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleGuideResponse;
import com.wedservice.backend.module.tours.dto.response.TourSchedulePickupPointResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;
import com.wedservice.backend.module.tours.dto.response.TourSeasonalityResponse;
import com.wedservice.backend.module.tours.entity.QTour;
import com.wedservice.backend.module.tours.entity.CancellationPolicy;
import com.wedservice.backend.module.tours.entity.CancellationPolicyRule;
import com.wedservice.backend.module.tours.entity.Guide;
import com.wedservice.backend.module.tours.entity.Tag;
import com.wedservice.backend.module.tours.entity.TourChecklistItem;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSeasonality;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleGuide;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.ItineraryItemRepository;
import com.wedservice.backend.module.tours.repository.CancellationPolicyRepository;
import com.wedservice.backend.module.tours.repository.CancellationPolicyRuleRepository;
import com.wedservice.backend.module.tours.repository.GuideRepository;
import com.wedservice.backend.module.tours.repository.TagRepository;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourChecklistItemRepository;
import com.wedservice.backend.module.tours.repository.TourItineraryDayRepository;
import com.wedservice.backend.module.tours.repository.TourMediaRepository;
import com.wedservice.backend.module.tours.repository.TourSeasonalityRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleGuideRepository;
import com.wedservice.backend.module.tours.repository.TourSchedulePickupPointRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import com.wedservice.backend.module.tours.repository.TourTagRepository;
import com.wedservice.backend.module.tours.service.query.TourQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TourQueryServiceImpl implements TourQueryService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "basePrice",
            "durationDays",
            "averageRating",
            "totalBookings",
            "createdAt"
    );
    private static final Set<TourScheduleStatus> PUBLIC_SCHEDULE_STATUSES = Set.of(
            TourScheduleStatus.OPEN,
            TourScheduleStatus.CLOSED,
            TourScheduleStatus.FULL,
            TourScheduleStatus.DEPARTED,
            TourScheduleStatus.COMPLETED
    );

    private final TourRepository tourRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final CancellationPolicyRuleRepository cancellationPolicyRuleRepository;
    private final GuideRepository guideRepository;
    private final TourMediaRepository tourMediaRepository;
    private final TagRepository tagRepository;
    private final TourTagRepository tourTagRepository;
    private final TourSeasonalityRepository tourSeasonalityRepository;
    private final TourItineraryDayRepository tourItineraryDayRepository;
    private final ItineraryItemRepository itineraryItemRepository;
    private final TourChecklistItemRepository tourChecklistItemRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourSchedulePickupPointRepository tourSchedulePickupPointRepository;
    private final TourScheduleGuideRepository tourScheduleGuideRepository;

    @Override
    @Cacheable(value = "tours", key = "#request")
    public Page<TourResponse> searchTours(TourSearchRequest request) {
        validateSearchRequest(request);
        PageRequest pr = PageRequest.of(request.getPage(), request.getSize(), buildSort(request));

        QTour qTour = QTour.tour;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTour.deletedAt.isNull());
        builder.and(qTour.status.eq(TourStatus.ACTIVE));

        if (request.getDestinationId() != null) {
            builder.and(qTour.destination.id.eq(request.getDestinationId()));
        }
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            builder.and(qTour.name.containsIgnoreCase(keyword)
                    .or(qTour.slug.containsIgnoreCase(keyword))
                    .or(qTour.shortDescription.containsIgnoreCase(keyword))
                    .or(qTour.description.containsIgnoreCase(keyword))
                    .or(qTour.highlights.containsIgnoreCase(keyword)));
        }
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Long> matchingTourIds = resolveTourIdsByTagIds(request.getTagIds());
            if (matchingTourIds.isEmpty()) {
                return Page.empty(pr);
            }
            builder.and(qTour.id.in(matchingTourIds));
        }
        if (request.getMinPrice() != null) {
            builder.and(qTour.basePrice.goe(request.getMinPrice()));
        }
        if (request.getMaxPrice() != null) {
            builder.and(qTour.basePrice.loe(request.getMaxPrice()));
        }
        if (request.getTravelMonth() != null) {
            List<Long> matchingTourIds = resolveTourIdsByTravelMonth(request.getTravelMonth());
            if (matchingTourIds.isEmpty()) {
                return Page.empty(pr);
            }
            builder.and(qTour.id.in(matchingTourIds));
        }
        if (Boolean.TRUE.equals(request.getFeaturedOnly())) {
            builder.and(qTour.isFeatured.isTrue());
        }
        if (Boolean.TRUE.equals(request.getStudentFriendlyOnly())) {
            builder.and(qTour.isStudentFriendly.isTrue());
        }
        if (Boolean.TRUE.equals(request.getFamilyFriendlyOnly())) {
            builder.and(qTour.isFamilyFriendly.isTrue());
        }
        if (Boolean.TRUE.equals(request.getSeniorFriendlyOnly())) {
            builder.and(qTour.isSeniorFriendly.isTrue());
        }
        if (request.getDifficultyLevel() != null) {
            builder.and(qTour.difficultyLevel.eq(request.getDifficultyLevel()));
        }
        if (request.getActivityLevel() != null) {
            builder.and(qTour.activityLevel.eq(request.getActivityLevel()));
        }
        if (request.getMinDurationDays() != null) {
            builder.and(qTour.durationDays.goe(request.getMinDurationDays()));
        }
        if (request.getMaxDurationDays() != null) {
            builder.and(qTour.durationDays.loe(request.getMaxDurationDays()));
        }
        if (request.getTravellerAge() != null) {
            builder.and(qTour.minAge.isNull().or(qTour.minAge.loe(request.getTravellerAge())));
            builder.and(qTour.maxAge.isNull().or(qTour.maxAge.goe(request.getTravellerAge())));
        }
        if (request.getGroupSize() != null) {
            builder.and(qTour.minGroupSize.loe(request.getGroupSize()));
            builder.and(qTour.maxGroupSize.goe(request.getGroupSize()));
        }
        if (StringUtils.hasText(request.getTripMode())) {
            builder.and(qTour.tripMode.equalsIgnoreCase(request.getTripMode().trim()));
        }
        if (StringUtils.hasText(request.getTransportType())) {
            builder.and(qTour.transportType.containsIgnoreCase(request.getTransportType().trim()));
        }
        if (request.getMinRating() != null) {
            builder.and(qTour.averageRating.goe(request.getMinRating()));
        }

        Page<Tour> page = tourRepository.findAll(builder, pr);
        return page.map(this::toResponse);
    }

    @Override
    @Cacheable(value = "tour-details", key = "#id")
    public TourResponse getTour(Long id) {
        Tour tour = findActiveTour(id);
        return toResponse(tour, true);
    }

    @Override
    @Cacheable(value = "tour-schedules", key = "'public:' + #tourId")
    public List<TourScheduleResponse> getTourSchedules(Long tourId) {
        findActiveTour(tourId);
        return tourScheduleRepository.findByTourId(tourId).stream()
                .filter(schedule -> schedule.getDeletedAt() == null)
                .filter(schedule -> PUBLIC_SCHEDULE_STATUSES.contains(schedule.getStatus()))
                .map(this::toScheduleResponse)
                .toList();
    }

    @Override
    public List<TourScheduleResponse> getAdminTourSchedules(Long tourId) {
        findActiveTour(tourId);
        return tourScheduleRepository.findByTourId(tourId).stream()
                .filter(schedule -> schedule.getDeletedAt() == null)
                .map(this::toScheduleResponse)
                .toList();
    }

    @Override
    public TourScheduleResponse getTourSchedule(Long tourId, Long scheduleId) {
        findActiveTour(tourId);
        TourSchedule schedule = tourScheduleRepository.findByIdAndTourId(scheduleId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour schedule not found"));
        if (schedule.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Tour schedule has been deleted");
        }
        return toScheduleResponse(schedule);
    }

    private Tour findActiveTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        if (tour.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Tour has been deleted");
        }
        return tour;
    }

    private List<Long> resolveTourIdsByTagIds(Collection<Long> tagIds) {
        return new java.util.ArrayList<>(tourTagRepository.findByIdTagIdIn(tagIds).stream()
                .map(tourTag -> tourTag.getId().getTourId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
    }

    private List<Long> resolveTourIdsByTravelMonth(Integer travelMonth) {
        return new java.util.ArrayList<>(tourSeasonalityRepository.findActiveByTravelMonth(travelMonth).stream()
                .map(TourSeasonality::getTourId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
    }

    private void validateSearchRequest(TourSearchRequest request) {
        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();
        if (minPrice != null && maxPrice != null && maxPrice.compareTo(minPrice) < 0) {
            throw new BadRequestException("maxPrice must be greater than or equal to minPrice");
        }
        Integer minDurationDays = request.getMinDurationDays();
        Integer maxDurationDays = request.getMaxDurationDays();
        if (minDurationDays != null && maxDurationDays != null && maxDurationDays < minDurationDays) {
            throw new BadRequestException("maxDurationDays must be greater than or equal to minDurationDays");
        }
    }

    private Sort buildSort(TourSearchRequest request) {
        String sortBy = StringUtils.hasText(request.getSortBy()) && ALLOWED_SORT_FIELDS.contains(request.getSortBy())
                ? request.getSortBy()
                : "createdAt";
        Sort.Direction direction = StringUtils.hasText(request.getSortDir())
                ? Sort.Direction.fromString(request.getSortDir())
                : Sort.Direction.DESC;
        return Sort.by(direction, sortBy);
    }

    private TourResponse toResponse(Tour t, boolean includeContent) {
        TourResponse.TourResponseBuilder builder = TourResponse.builder()
                .id(t.getId())
                .code(t.getCode())
                .name(t.getName())
                .slug(t.getSlug())
                .destinationId(t.getDestination() != null ? t.getDestination().getId() : null)
                .cancellationPolicyId(t.getCancellationPolicyId())
                .basePrice(t.getBasePrice())
                .currency(t.getCurrency())
                .durationDays(t.getDurationDays())
                .durationNights(t.getDurationNights())
                .shortDescription(t.getShortDescription())
                .description(t.getDescription())
                .transportType(t.getTransportType())
                .tripMode(t.getTripMode())
                .highlights(t.getHighlights())
                .inclusions(t.getInclusions())
                .exclusions(t.getExclusions())
                .notes(t.getNotes())
                .isFeatured(t.getIsFeatured())
                .status(t.getStatus() != null ? t.getStatus().getValue() : null);

        if (includeContent) {
            builder.tags(loadTagResponses(t.getId()))
                    .media(loadMediaResponses(t.getId()))
                    .seasonality(loadSeasonalityResponses(t.getId()))
                    .itineraryDays(loadItineraryDayResponses(t.getId()))
                    .checklistItems(loadChecklistResponses(t.getId()))
                    .cancellationPolicy(loadCancellationPolicyResponse(t.getCancellationPolicyId()));
        }

        return builder.build();
    }

    private TourResponse toResponse(Tour t) {
        return toResponse(t, false);
    }

    private TourScheduleResponse toScheduleResponse(TourSchedule schedule) {
        List<TourSchedulePickupPointResponse> pickupPoints = tourSchedulePickupPointRepository
                .findByScheduleIdOrderBySortOrder(schedule.getId())
                .stream()
                .map(pickupPoint -> TourSchedulePickupPointResponse.builder()
                        .id(pickupPoint.getId())
                        .pointName(pickupPoint.getPointName())
                        .address(pickupPoint.getAddress())
                        .latitude(pickupPoint.getLatitude())
                        .longitude(pickupPoint.getLongitude())
                        .pickupAt(pickupPoint.getPickupAt())
                        .sortOrder(pickupPoint.getSortOrder())
                        .build())
                .toList();

        List<TourScheduleGuide> assignedGuides = tourScheduleGuideRepository.findByScheduleId(schedule.getId());
        Map<Long, Guide> guideMap = loadGuideMap(assignedGuides.stream().map(TourScheduleGuide::getGuideId).toList());
        List<TourScheduleGuideResponse> guideAssignments = assignedGuides
                .stream()
                .map(guide -> TourScheduleGuideResponse.builder()
                        .id(guide.getId())
                        .guideId(guide.getGuideId())
                        .guideCode(guideMap.containsKey(guide.getGuideId()) ? guideMap.get(guide.getGuideId()).getCode() : null)
                        .guideFullName(guideMap.containsKey(guide.getGuideId()) ? guideMap.get(guide.getGuideId()).getFullName() : null)
                        .guidePhone(guideMap.containsKey(guide.getGuideId()) ? guideMap.get(guide.getGuideId()).getPhone() : null)
                        .guideEmail(guideMap.containsKey(guide.getGuideId()) ? guideMap.get(guide.getGuideId()).getEmail() : null)
                        .guideStatus(guideMap.containsKey(guide.getGuideId()) ? guideMap.get(guide.getGuideId()).getStatus() : null)
                        .isLocalGuide(guideMap.containsKey(guide.getGuideId()) ? guideMap.get(guide.getGuideId()).getIsLocalGuide() : null)
                        .guideRole(guide.getGuideRole())
                        .assignedAt(guide.getAssignedAt())
                        .build())
                .toList();

        return TourScheduleResponse.builder()
                .id(schedule.getId())
                .scheduleCode(schedule.getScheduleCode())
                .tourId(schedule.getTourId())
                .departureAt(schedule.getDepartureAt())
                .returnAt(schedule.getReturnAt())
                .bookingOpenAt(schedule.getBookingOpenAt())
                .bookingCloseAt(schedule.getBookingCloseAt())
                .meetingAt(schedule.getMeetingAt())
                .meetingPointName(schedule.getMeetingPointName())
                .meetingAddress(schedule.getMeetingAddress())
                .meetingLatitude(schedule.getMeetingLatitude())
                .meetingLongitude(schedule.getMeetingLongitude())
                .capacityTotal(schedule.getCapacityTotal())
                .bookedSeats(schedule.getBookedSeats())
                .remainingSeats(schedule.getRemainingSeats())
                .minGuestsToOperate(schedule.getMinGuestsToOperate())
                .adultPrice(schedule.getAdultPrice())
                .childPrice(schedule.getChildPrice())
                .infantPrice(schedule.getInfantPrice())
                .seniorPrice(schedule.getSeniorPrice())
                .singleRoomSurcharge(schedule.getSingleRoomSurcharge())
                .transportDetail(schedule.getTransportDetail())
                .note(schedule.getNote())
                .status(schedule.getStatus().getValue())
                .pickupPoints(pickupPoints)
                .guideAssignments(guideAssignments)
                .build();
    }

    private List<TourMediaResponse> loadMediaResponses(Long tourId) {
        return tourMediaRepository.findByTourIdOrderBySortOrder(tourId).stream()
                .filter(media -> media.getDeletedAt() == null)
                .map(media -> TourMediaResponse.builder()
                        .id(media.getId())
                        .mediaType(media.getMediaType())
                        .mediaUrl(media.getMediaUrl())
                        .altText(media.getAltText())
                        .sortOrder(media.getSortOrder())
                        .isActive(media.getIsActive())
                        .build())
                .toList();
    }

    private List<TagResponse> loadTagResponses(Long tourId) {
        List<Long> tagIds = tourTagRepository.findByIdTourId(tourId).stream()
                .map(tourTag -> tourTag.getId().getTagId())
                .toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagRepository.findAllById(tagIds).stream()
                .filter(tag -> Boolean.TRUE.equals(tag.getIsActive()))
                .map(this::toTagResponse)
                .toList();
    }

    private TagResponse toTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .code(tag.getCode())
                .name(tag.getName())
                .tagGroup(tag.getTagGroup())
                .description(tag.getDescription())
                .build();
    }

    private List<TourSeasonalityResponse> loadSeasonalityResponses(Long tourId) {
        return tourSeasonalityRepository.findByTourId(tourId).stream()
                .filter(season -> season.getDeletedAt() == null)
                .sorted(Comparator.comparing(TourSeasonality::getSeasonName))
                .map(season -> TourSeasonalityResponse.builder()
                        .id(season.getId())
                        .seasonName(season.getSeasonName())
                        .monthFrom(season.getMonthFrom())
                        .monthTo(season.getMonthTo())
                        .recommendationScore(season.getRecommendationScore())
                        .notes(season.getNotes())
                        .build())
                .toList();
    }

    private List<TourItineraryDayResponse> loadItineraryDayResponses(Long tourId) {
        return tourItineraryDayRepository.findByTourIdOrderByDayNumber(tourId).stream()
                .filter(day -> day.getDeletedAt() == null)
                .map(day -> TourItineraryDayResponse.builder()
                        .id(day.getId())
                        .dayNumber(day.getDayNumber())
                        .title(day.getTitle())
                        .description(day.getDescription())
                        .overnightDestinationId(day.getOvernightDestinationId())
                        .items(itineraryItemRepository.findByItineraryDayIdOrderBySequenceNo(day.getId()).stream()
                                .filter(item -> item.getDeletedAt() == null)
                                .map(item -> ItineraryItemResponse.builder()
                                        .id(item.getId())
                                        .sequenceNo(item.getSequenceNo())
                                        .itemType(item.getItemType())
                                        .title(item.getTitle())
                                        .description(item.getDescription())
                                        .destinationId(item.getDestinationId())
                                        .locationName(item.getLocationName())
                                        .address(item.getAddress())
                                        .latitude(item.getLatitude())
                                        .longitude(item.getLongitude())
                                        .googleMapUrl(item.getGoogleMapUrl())
                                        .startTime(item.getStartTime())
                                        .endTime(item.getEndTime())
                                        .travelMinutesEstimated(item.getTravelMinutesEstimated())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    private List<TourChecklistItemResponse> loadChecklistResponses(Long tourId) {
        return tourChecklistItemRepository.findByTourId(tourId).stream()
                .filter(item -> item.getDeletedAt() == null)
                .sorted(Comparator
                        .comparing((TourChecklistItem item) -> item.getItemGroup() == null ? "" : item.getItemGroup())
                        .thenComparing(TourChecklistItem::getItemName))
                .map(item -> TourChecklistItemResponse.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .itemGroup(item.getItemGroup())
                        .isRequired(item.getIsRequired())
                        .build())
                .toList();
    }

    private CancellationPolicyResponse loadCancellationPolicyResponse(Long cancellationPolicyId) {
        if (cancellationPolicyId == null) {
            return null;
        }
        CancellationPolicy policy = cancellationPolicyRepository.findById(cancellationPolicyId)
                .orElse(null);
        if (policy == null) {
            return null;
        }
        return CancellationPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .voucherBonusPercent(policy.getVoucherBonusPercent())
                .isDefault(policy.getIsDefault())
                .isActive(policy.getIsActive())
                .rules(cancellationPolicyRuleRepository.findByPolicyIdOrderByMinHoursBeforeDesc(cancellationPolicyId).stream()
                        .map(this::toCancellationPolicyRuleResponse)
                        .toList())
                .build();
    }

    private CancellationPolicyRuleResponse toCancellationPolicyRuleResponse(CancellationPolicyRule rule) {
        return CancellationPolicyRuleResponse.builder()
                .id(rule.getId())
                .minHoursBefore(rule.getMinHoursBefore())
                .maxHoursBefore(rule.getMaxHoursBefore())
                .refundPercent(rule.getRefundPercent())
                .voucherPercent(rule.getVoucherPercent())
                .feePercent(rule.getFeePercent())
                .allowReschedule(rule.getAllowReschedule())
                .notes(rule.getNotes())
                .build();
    }

    private Map<Long, Guide> loadGuideMap(Collection<Long> guideIds) {
        if (guideIds == null || guideIds.isEmpty()) {
            return Map.of();
        }
        return guideRepository.findByIdIn(guideIds).stream()
                .collect(java.util.stream.Collectors.toMap(Guide::getId, guide -> guide, (left, right) -> left));
    }
}
