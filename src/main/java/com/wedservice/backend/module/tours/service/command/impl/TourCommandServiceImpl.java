package com.wedservice.backend.module.tours.service.command.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.tours.dto.request.ItineraryItemRequest;
import com.wedservice.backend.module.tours.dto.request.TourChecklistItemRequest;
import com.wedservice.backend.module.tours.dto.request.TourItineraryDayRequest;
import com.wedservice.backend.module.tours.dto.request.TourMediaRequest;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleGuideRequest;
import com.wedservice.backend.module.tours.dto.request.TourSchedulePickupPointRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleRequest;
import com.wedservice.backend.module.tours.dto.request.TourSeasonalityRequest;
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
import com.wedservice.backend.module.tours.entity.ItineraryItem;
import com.wedservice.backend.module.tours.entity.CancellationPolicy;
import com.wedservice.backend.module.tours.entity.CancellationPolicyRule;
import com.wedservice.backend.module.tours.entity.Guide;
import com.wedservice.backend.module.tours.entity.Tag;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourChecklistItem;
import com.wedservice.backend.module.tours.entity.TourItineraryDay;
import com.wedservice.backend.module.tours.entity.TourMedia;
import com.wedservice.backend.module.tours.entity.TourSeasonality;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleGuide;
import com.wedservice.backend.module.tours.entity.TourSchedulePickupPoint;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.entity.TourTag;
import com.wedservice.backend.module.tours.entity.TourTagId;
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
import com.wedservice.backend.module.tours.service.command.TourCommandService;
import com.wedservice.backend.module.tours.validator.TourValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TourCommandServiceImpl implements TourCommandService {

    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
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
    private final TourValidator tourValidator;

    @Override
    @Transactional
    public TourResponse createTour(TourRequest request) {
        tourValidator.validateRequest(request);
        Destination destination = findDestination(request.getDestinationId());
        CancellationPolicy cancellationPolicy = resolveCancellationPolicy(request.getCancellationPolicyId());

        Tour t = Tour.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .slug(request.getSlug().trim())
                .destination(destination)
                .cancellationPolicyId(cancellationPolicy.getId())
                .basePrice(request.getBasePrice())
                .currency(tourValidator.normalizeCurrency(request.getCurrency()))
                .durationDays(request.getDurationDays())
                .durationNights(request.getDurationNights() == null ? 0 : request.getDurationNights())
                .shortDescription(DataNormalizer.normalize(request.getShortDescription()))
                .description(DataNormalizer.normalize(request.getDescription()))
                .transportType(request.getTransportType())
                .tripMode(request.getTripMode())
                .highlights(request.getHighlights())
                .inclusions(request.getInclusions())
                .exclusions(request.getExclusions())
                .notes(request.getNotes())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .status(tourValidator.normalizeStatus(request.getStatus()))
                .build();

        t = tourRepository.save(t);
        syncTourContent(t.getId(), request);
        return toResponse(t, true);
    }

    @Override
    @Transactional
    public TourResponse updateTour(Long id, TourRequest request) {
        tourValidator.validateRequest(request);
        Tour t = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        Destination destination = findDestination(request.getDestinationId());
        CancellationPolicy cancellationPolicy = resolveCancellationPolicy(request.getCancellationPolicyId());

        t.setCode(request.getCode().trim());
        t.setName(request.getName().trim());
        t.setSlug(request.getSlug().trim());
        t.setDestination(destination);
        t.setCancellationPolicyId(cancellationPolicy.getId());
        t.setBasePrice(request.getBasePrice());
        t.setCurrency(tourValidator.normalizeCurrency(request.getCurrency()));
        t.setDurationDays(request.getDurationDays());
        t.setDurationNights(request.getDurationNights() == null ? 0 : request.getDurationNights());
        t.setShortDescription(DataNormalizer.normalize(request.getShortDescription()));
        t.setDescription(DataNormalizer.normalize(request.getDescription()));
        t.setTransportType(request.getTransportType());
        t.setTripMode(request.getTripMode());
        t.setHighlights(request.getHighlights());
        t.setInclusions(request.getInclusions());
        t.setExclusions(request.getExclusions());
        t.setNotes(request.getNotes());
        t.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        t.setStatus(tourValidator.normalizeStatus(request.getStatus()));

        t = tourRepository.save(t);
        syncTourContent(t.getId(), request);
        return toResponse(t, true);
    }

    @Override
    public void deleteTour(Long id) {
        tourRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TourScheduleResponse createTourSchedule(Long tourId, TourScheduleRequest request) {
        tourValidator.validateScheduleRequest(request);
        Tour tour = findTour(tourId);

        TourSchedule schedule = TourSchedule.builder()
                .scheduleCode(normalizeScheduleCode(request.getScheduleCode()))
                .tourId(tour.getId())
                .departureAt(request.getDepartureAt())
                .returnAt(request.getReturnAt())
                .bookingOpenAt(request.getBookingOpenAt())
                .bookingCloseAt(request.getBookingCloseAt())
                .meetingAt(request.getMeetingAt())
                .meetingPointName(DataNormalizer.normalize(request.getMeetingPointName()))
                .meetingAddress(DataNormalizer.normalize(request.getMeetingAddress()))
                .meetingLatitude(request.getMeetingLatitude())
                .meetingLongitude(request.getMeetingLongitude())
                .capacityTotal(request.getCapacityTotal())
                .minGuestsToOperate(request.getMinGuestsToOperate() == null ? 1 : request.getMinGuestsToOperate())
                .adultPrice(defaultPrice(request.getAdultPrice()))
                .childPrice(defaultPrice(request.getChildPrice()))
                .infantPrice(defaultPrice(request.getInfantPrice()))
                .seniorPrice(defaultPrice(request.getSeniorPrice()))
                .singleRoomSurcharge(defaultPrice(request.getSingleRoomSurcharge()))
                .transportDetail(DataNormalizer.normalize(request.getTransportDetail()))
                .note(DataNormalizer.normalize(request.getNote()))
                .status(tourValidator.normalizeScheduleStatus(request.getStatus()))
                .build();

        schedule = tourScheduleRepository.save(schedule);
        syncScheduleChildren(schedule.getId(), request.getPickupPoints(), request.getGuideAssignments());
        return buildScheduleResponse(schedule);
    }

    @Override
    @Transactional
    public TourScheduleResponse updateTourSchedule(Long tourId, Long scheduleId, TourScheduleRequest request) {
        tourValidator.validateScheduleRequest(request);
        findTour(tourId);
        TourSchedule schedule = findSchedule(tourId, scheduleId);

        schedule.setScheduleCode(normalizeScheduleCode(request.getScheduleCode(), schedule.getScheduleCode()));
        schedule.setDepartureAt(request.getDepartureAt());
        schedule.setReturnAt(request.getReturnAt());
        schedule.setBookingOpenAt(request.getBookingOpenAt());
        schedule.setBookingCloseAt(request.getBookingCloseAt());
        schedule.setMeetingAt(request.getMeetingAt());
        schedule.setMeetingPointName(DataNormalizer.normalize(request.getMeetingPointName()));
        schedule.setMeetingAddress(DataNormalizer.normalize(request.getMeetingAddress()));
        schedule.setMeetingLatitude(request.getMeetingLatitude());
        schedule.setMeetingLongitude(request.getMeetingLongitude());
        schedule.setCapacityTotal(request.getCapacityTotal());
        schedule.setMinGuestsToOperate(request.getMinGuestsToOperate() == null ? 1 : request.getMinGuestsToOperate());
        schedule.setAdultPrice(defaultPrice(request.getAdultPrice()));
        schedule.setChildPrice(defaultPrice(request.getChildPrice()));
        schedule.setInfantPrice(defaultPrice(request.getInfantPrice()));
        schedule.setSeniorPrice(defaultPrice(request.getSeniorPrice()));
        schedule.setSingleRoomSurcharge(defaultPrice(request.getSingleRoomSurcharge()));
        schedule.setTransportDetail(DataNormalizer.normalize(request.getTransportDetail()));
        schedule.setNote(DataNormalizer.normalize(request.getNote()));

        TourScheduleStatus targetStatus = request.getStatus() == null
                ? schedule.getStatus()
                : tourValidator.normalizeScheduleStatus(request.getStatus());
        tourValidator.validateStatusUpdate(schedule, targetStatus);
        schedule.setStatus(targetStatus);

        schedule = tourScheduleRepository.save(schedule);
        syncScheduleChildren(schedule.getId(), request.getPickupPoints(), request.getGuideAssignments());
        return buildScheduleResponse(schedule);
    }

    @Override
    @Transactional
    public TourScheduleResponse updateTourScheduleStatus(Long tourId, Long scheduleId, String status) {
        findTour(tourId);
        TourSchedule schedule = findSchedule(tourId, scheduleId);
        TourScheduleStatus targetStatus = tourValidator.normalizeScheduleStatus(status);
        tourValidator.validateStatusUpdate(schedule, targetStatus);
        if (targetStatus == TourScheduleStatus.OPEN
                && schedule.getCapacityTotal() != null
                && schedule.getBookedSeats() != null
                && schedule.getBookedSeats() >= schedule.getCapacityTotal()) {
            targetStatus = TourScheduleStatus.FULL;
        }
        schedule.setStatus(targetStatus);
        schedule = tourScheduleRepository.save(schedule);
        return buildScheduleResponse(schedule);
    }

    private Destination findDestination(Long destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + destinationId));
        if (destination.getDeletedAt() != null) {
            throw new BadRequestException("Destination has been deleted");
        }
        return destination;
    }

    private Tour findTour(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + tourId));
        if (tour.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Tour has been deleted");
        }
        return tour;
    }

    private CancellationPolicy resolveCancellationPolicy(Long cancellationPolicyId) {
        CancellationPolicy policy = cancellationPolicyId == null
                ? cancellationPolicyRepository.findFirstByIsDefaultTrueAndIsActiveTrue()
                .orElseThrow(() -> new BadRequestException("Default active cancellation policy is not configured"))
                : cancellationPolicyRepository.findByIdAndIsActiveTrue(cancellationPolicyId)
                .orElseThrow(() -> new BadRequestException("Cancellation policy is invalid or inactive"));
        if (!cancellationPolicyRuleRepository.existsByPolicyId(policy.getId())) {
            throw new BadRequestException("Cancellation policy must have at least one rule");
        }
        return policy;
    }

    private TourSchedule findSchedule(Long tourId, Long scheduleId) {
        TourSchedule schedule = tourScheduleRepository.findByIdAndTourId(scheduleId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour schedule not found"));
        if (schedule.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Tour schedule has been deleted");
        }
        return schedule;
    }

    private void syncScheduleChildren(
            Long scheduleId,
            List<TourSchedulePickupPointRequest> pickupPoints,
            List<TourScheduleGuideRequest> guideAssignments
    ) {
        List<TourSchedulePickupPoint> existingPickupPoints = tourSchedulePickupPointRepository.findByScheduleIdOrderBySortOrder(scheduleId);
        if (!existingPickupPoints.isEmpty()) {
            tourSchedulePickupPointRepository.deleteAll(existingPickupPoints);
        }
        List<TourScheduleGuide> existingGuideAssignments = tourScheduleGuideRepository.findByScheduleId(scheduleId);
        if (!existingGuideAssignments.isEmpty()) {
            tourScheduleGuideRepository.deleteAll(existingGuideAssignments);
        }

        if (pickupPoints != null && !pickupPoints.isEmpty()) {
            List<TourSchedulePickupPoint> pickupPointEntities = pickupPoints.stream()
                    .map(pickupPoint -> TourSchedulePickupPoint.builder()
                            .scheduleId(scheduleId)
                            .pointName(DataNormalizer.normalize(pickupPoint.getPointName()))
                            .address(DataNormalizer.normalize(pickupPoint.getAddress()))
                            .latitude(pickupPoint.getLatitude())
                            .longitude(pickupPoint.getLongitude())
                            .pickupAt(pickupPoint.getPickupAt())
                            .sortOrder(pickupPoint.getSortOrder() == null ? 0 : pickupPoint.getSortOrder())
                            .build())
                    .toList();
            tourSchedulePickupPointRepository.saveAll(pickupPointEntities);
        }

        if (guideAssignments != null && !guideAssignments.isEmpty()) {
            Map<Long, Guide> guideMap = resolveActiveGuides(guideAssignments.stream()
                    .map(TourScheduleGuideRequest::getGuideId)
                    .toList());
            LocalDateTime assignedAt = LocalDateTime.now();
            List<TourScheduleGuide> guideEntities = guideAssignments.stream()
                    .map(guide -> TourScheduleGuide.builder()
                            .scheduleId(scheduleId)
                            .guideId(guideMap.get(guide.getGuideId()).getId())
                            .guideRole(normalizeGuideRole(guide.getGuideRole()))
                            .assignedAt(assignedAt)
                            .build())
                    .toList();
            tourScheduleGuideRepository.saveAll(guideEntities);
        }
    }

    private void syncTourContent(Long tourId, TourRequest request) {
        syncTourTags(tourId, request.getTagIds());
        syncTourMedia(tourId, request.getMedia());
        syncTourSeasonality(tourId, request.getSeasonality());
        syncTourItinerary(tourId, request.getItineraryDays());
        syncTourChecklist(tourId, request.getChecklistItems());
    }

    private void syncTourTags(Long tourId, List<Long> tagIds) {
        List<TourTag> existingTags = tourTagRepository.findByIdTourId(tourId);
        if (!existingTags.isEmpty()) {
            tourTagRepository.deleteAll(existingTags);
        }
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Tag> activeTags = tagRepository.findByIdInAndIsActiveTrue(tagIds);
        if (activeTags.size() != tagIds.size()) {
            throw new BadRequestException("One or more tags are invalid or inactive");
        }
        List<TourTag> tourTags = tagIds.stream()
                .map(tagId -> TourTag.builder()
                        .id(TourTagId.builder().tourId(tourId).tagId(tagId).build())
                        .build())
                .toList();
        tourTagRepository.saveAll(tourTags);
    }

    private void syncTourMedia(Long tourId, List<TourMediaRequest> mediaRequests) {
        List<TourMedia> existingMedia = tourMediaRepository.findByTourIdOrderBySortOrder(tourId).stream()
                .filter(media -> media.getDeletedAt() == null)
                .toList();
        if (!existingMedia.isEmpty()) {
            tourMediaRepository.deleteAll(existingMedia);
        }
        if (mediaRequests == null || mediaRequests.isEmpty()) {
            return;
        }
        List<TourMedia> mediaEntities = mediaRequests.stream()
                .map(media -> TourMedia.builder()
                        .tourId(tourId)
                        .mediaType(DataNormalizer.normalize(media.getMediaType()))
                        .mediaUrl(DataNormalizer.normalize(media.getMediaUrl()))
                        .altText(DataNormalizer.normalize(media.getAltText()))
                        .sortOrder(media.getSortOrder() == null ? 0 : media.getSortOrder())
                        .isActive(media.getIsActive() == null || media.getIsActive())
                        .build())
                .toList();
        tourMediaRepository.saveAll(mediaEntities);
    }

    private void syncTourSeasonality(Long tourId, List<TourSeasonalityRequest> seasonalityRequests) {
        List<TourSeasonality> existingSeasonality = tourSeasonalityRepository.findByTourId(tourId).stream()
                .filter(season -> season.getDeletedAt() == null)
                .toList();
        if (!existingSeasonality.isEmpty()) {
            tourSeasonalityRepository.deleteAll(existingSeasonality);
        }
        if (seasonalityRequests == null || seasonalityRequests.isEmpty()) {
            return;
        }
        List<TourSeasonality> seasonalityEntities = seasonalityRequests.stream()
                .map(season -> TourSeasonality.builder()
                        .tourId(tourId)
                        .seasonName(DataNormalizer.normalize(season.getSeasonName()))
                        .monthFrom(season.getMonthFrom())
                        .monthTo(season.getMonthTo())
                        .recommendationScore(season.getRecommendationScore() == null ? BigDecimal.ZERO : season.getRecommendationScore())
                        .notes(DataNormalizer.normalize(season.getNotes()))
                        .build())
                .toList();
        tourSeasonalityRepository.saveAll(seasonalityEntities);
    }

    private void syncTourItinerary(Long tourId, List<TourItineraryDayRequest> itineraryDayRequests) {
        List<TourItineraryDay> existingDays = tourItineraryDayRepository.findByTourIdOrderByDayNumber(tourId).stream()
                .filter(day -> day.getDeletedAt() == null)
                .toList();
        if (!existingDays.isEmpty()) {
            List<Long> dayIds = existingDays.stream().map(TourItineraryDay::getId).toList();
            List<ItineraryItem> existingItems = dayIds.stream()
                    .flatMap(dayId -> itineraryItemRepository.findByItineraryDayIdOrderBySequenceNo(dayId).stream())
                    .filter(item -> item.getDeletedAt() == null)
                    .toList();
            if (!existingItems.isEmpty()) {
                itineraryItemRepository.deleteAll(existingItems);
            }
            tourItineraryDayRepository.deleteAll(existingDays);
        }
        if (itineraryDayRequests == null || itineraryDayRequests.isEmpty()) {
            return;
        }
        for (TourItineraryDayRequest dayRequest : itineraryDayRequests) {
            TourItineraryDay savedDay = tourItineraryDayRepository.save(TourItineraryDay.builder()
                    .tourId(tourId)
                    .dayNumber(dayRequest.getDayNumber())
                    .title(DataNormalizer.normalize(dayRequest.getTitle()))
                    .description(DataNormalizer.normalize(dayRequest.getDescription()))
                    .overnightDestinationId(dayRequest.getOvernightDestinationId())
                    .build());
            if (dayRequest.getItems() != null && !dayRequest.getItems().isEmpty()) {
                List<ItineraryItem> items = dayRequest.getItems().stream()
                        .map(item -> toItineraryItem(savedDay.getId(), item))
                        .toList();
                itineraryItemRepository.saveAll(items);
            }
        }
    }

    private ItineraryItem toItineraryItem(Long itineraryDayId, ItineraryItemRequest item) {
        return ItineraryItem.builder()
                .itineraryDayId(itineraryDayId)
                .sequenceNo(item.getSequenceNo())
                .itemType(DataNormalizer.normalize(item.getItemType()))
                .title(DataNormalizer.normalize(item.getTitle()))
                .description(DataNormalizer.normalize(item.getDescription()))
                .destinationId(item.getDestinationId())
                .locationName(DataNormalizer.normalize(item.getLocationName()))
                .address(DataNormalizer.normalize(item.getAddress()))
                .latitude(item.getLatitude())
                .longitude(item.getLongitude())
                .googleMapUrl(DataNormalizer.normalize(item.getGoogleMapUrl()))
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .travelMinutesEstimated(item.getTravelMinutesEstimated())
                .build();
    }

    private void syncTourChecklist(Long tourId, List<TourChecklistItemRequest> checklistItemRequests) {
        List<TourChecklistItem> existingChecklistItems = tourChecklistItemRepository.findByTourId(tourId).stream()
                .filter(item -> item.getDeletedAt() == null)
                .toList();
        if (!existingChecklistItems.isEmpty()) {
            tourChecklistItemRepository.deleteAll(existingChecklistItems);
        }
        if (checklistItemRequests == null || checklistItemRequests.isEmpty()) {
            return;
        }
        List<TourChecklistItem> checklistItems = checklistItemRequests.stream()
                .map(item -> TourChecklistItem.builder()
                        .tourId(tourId)
                        .itemName(DataNormalizer.normalize(item.getItemName()))
                        .itemGroup(DataNormalizer.normalize(item.getItemGroup()))
                        .isRequired(Boolean.TRUE.equals(item.getIsRequired()))
                        .build())
                .toList();
        tourChecklistItemRepository.saveAll(checklistItems);
    }

    private TourScheduleResponse buildScheduleResponse(TourSchedule schedule) {
        List<TourSchedulePickupPointResponse> pickupPointResponses = tourSchedulePickupPointRepository
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
        List<TourScheduleGuideResponse> guideResponses = assignedGuides
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
                .pickupPoints(pickupPointResponses)
                .guideAssignments(guideResponses)
                .build();
    }

    private String normalizeScheduleCode(String rawValue) {
        String normalized = DataNormalizer.normalize(rawValue);
        return StringUtils.hasText(normalized) ? normalized : "SCH" + System.currentTimeMillis();
    }

    private String normalizeScheduleCode(String rawValue, String fallback) {
        String normalized = DataNormalizer.normalize(rawValue);
        return StringUtils.hasText(normalized) ? normalized : fallback;
    }

    private BigDecimal defaultPrice(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalizeGuideRole(String rawValue) {
        String normalized = DataNormalizer.normalize(rawValue);
        return StringUtils.hasText(normalized) ? normalized : "main";
    }

    private Map<Long, Guide> resolveActiveGuides(Collection<Long> guideIds) {
        Map<Long, Guide> guideMap = loadGuideMap(guideIds);
        if (guideMap.size() != guideIds.size()) {
            throw new BadRequestException("One or more guides do not exist");
        }
        boolean hasUnavailableGuide = guideMap.values().stream()
                .anyMatch(guide -> !"active".equalsIgnoreCase(guide.getStatus()));
        if (hasUnavailableGuide) {
            throw new BadRequestException("Only active guides can be assigned to schedules");
        }
        return guideMap;
    }

    private Map<Long, Guide> loadGuideMap(Collection<Long> guideIds) {
        if (guideIds == null || guideIds.isEmpty()) {
            return Map.of();
        }
        return guideRepository.findByIdIn(guideIds).stream()
                .collect(java.util.stream.Collectors.toMap(Guide::getId, guide -> guide, (left, right) -> left, LinkedHashMap::new));
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

    private TourResponse toResponse(Tour t) {
        return TourResponse.builder()
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
                .status(t.getStatus() != null ? t.getStatus().getValue() : null)
                .build();
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
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .code(tag.getCode())
                        .name(tag.getName())
                        .tagGroup(tag.getTagGroup())
                        .description(tag.getDescription())
                        .build())
                .toList();
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

    private CancellationPolicyResponse loadCancellationPolicyResponse(Long cancellationPolicyId) {
        if (cancellationPolicyId == null) {
            return null;
        }
        CancellationPolicy policy = cancellationPolicyRepository.findById(cancellationPolicyId)
                .orElse(null);
        if (policy == null) {
            return null;
        }
        List<CancellationPolicyRuleResponse> rules = cancellationPolicyRuleRepository
                .findByPolicyIdOrderByMinHoursBeforeDesc(cancellationPolicyId)
                .stream()
                .map(this::toCancellationPolicyRuleResponse)
                .toList();
        return CancellationPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .voucherBonusPercent(policy.getVoucherBonusPercent())
                .isDefault(policy.getIsDefault())
                .isActive(policy.getIsActive())
                .rules(rules)
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
}
