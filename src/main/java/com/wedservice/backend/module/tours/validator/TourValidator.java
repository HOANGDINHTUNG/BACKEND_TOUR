package com.wedservice.backend.module.tours.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.tours.dto.request.ItineraryItemRequest;
import com.wedservice.backend.module.tours.dto.request.TourChecklistItemRequest;
import com.wedservice.backend.module.tours.dto.request.TourItineraryDayRequest;
import com.wedservice.backend.module.tours.dto.request.TourMediaRequest;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourSeasonalityRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleGuideRequest;
import com.wedservice.backend.module.tours.dto.request.TourSchedulePickupPointRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleRequest;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.entity.TourStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class TourValidator {

    public void validateRequest(TourRequest request) {
        if (request.getDurationDays() != null
                && request.getDurationNights() != null
                && request.getDurationNights() > request.getDurationDays()) {
            throw new BadRequestException("Duration nights cannot be greater than duration days");
        }
        validateMedia(request.getMedia());
        validateTagIds(request.getTagIds());
        validateSeasonality(request.getSeasonality());
        validateItineraryDays(request.getItineraryDays(), request.getDurationDays());
        validateChecklistItems(request.getChecklistItems());
    }

    public String normalizeCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return "VND";
        }
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 3) {
            throw new BadRequestException("Currency must be a 3-character code");
        }
        return normalized;
    }

    public TourStatus normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return TourStatus.DRAFT;
        }
        try {
            return TourStatus.fromValue(status.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public void validateScheduleRequest(TourScheduleRequest request) {
        if (request.getReturnAt() != null && request.getDepartureAt() != null
                && !request.getReturnAt().isAfter(request.getDepartureAt())) {
            throw new BadRequestException("Schedule returnAt must be after departureAt");
        }
        if (request.getBookingOpenAt() != null && request.getBookingCloseAt() != null
                && request.getBookingCloseAt().isBefore(request.getBookingOpenAt())) {
            throw new BadRequestException("Schedule bookingCloseAt must be after bookingOpenAt");
        }
        if (request.getBookingCloseAt() != null && request.getDepartureAt() != null
                && request.getBookingCloseAt().isAfter(request.getDepartureAt())) {
            throw new BadRequestException("Schedule bookingCloseAt must not be after departureAt");
        }
        if (request.getMeetingAt() != null && request.getDepartureAt() != null
                && request.getMeetingAt().isAfter(request.getDepartureAt())) {
            throw new BadRequestException("Schedule meetingAt must not be after departureAt");
        }
        if (request.getMinGuestsToOperate() != null && request.getCapacityTotal() != null
                && request.getMinGuestsToOperate() > request.getCapacityTotal()) {
            throw new BadRequestException("Schedule minGuestsToOperate must not exceed capacityTotal");
        }

        validateNonNegativePrice(request.getAdultPrice(), "adultPrice");
        validateNonNegativePrice(request.getChildPrice(), "childPrice");
        validateNonNegativePrice(request.getInfantPrice(), "infantPrice");
        validateNonNegativePrice(request.getSeniorPrice(), "seniorPrice");
        validateNonNegativePrice(request.getSingleRoomSurcharge(), "singleRoomSurcharge");

        if (request.getPickupPoints() != null) {
            for (TourSchedulePickupPointRequest pickupPoint : request.getPickupPoints()) {
                if (pickupPoint.getPickupAt() != null && request.getDepartureAt() != null
                        && pickupPoint.getPickupAt().isAfter(request.getDepartureAt())) {
                    throw new BadRequestException("Pickup point time must not be after departureAt");
                }
            }
        }

        if (request.getGuideAssignments() != null) {
            Set<Long> guideIds = new HashSet<>();
            for (TourScheduleGuideRequest guideAssignment : request.getGuideAssignments()) {
                if (guideAssignment.getGuideId() != null && guideAssignment.getGuideId() <= 0) {
                    throw new BadRequestException("guideId must be greater than 0");
                }
                if (guideAssignment.getGuideId() != null && !guideIds.add(guideAssignment.getGuideId())) {
                    throw new BadRequestException("guideAssignments must not contain duplicate guideId");
                }
            }
        }
    }

    public TourScheduleStatus normalizeScheduleStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return TourScheduleStatus.DRAFT;
        }
        try {
            return TourScheduleStatus.fromValue(status.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public void validateStatusUpdate(TourSchedule schedule, TourScheduleStatus targetStatus) {
        if (targetStatus == TourScheduleStatus.DRAFT
                && schedule.getBookedSeats() != null
                && schedule.getBookedSeats() > 0) {
            throw new BadRequestException("Schedule with booked seats cannot move back to draft");
        }
        if ((targetStatus == TourScheduleStatus.OPEN || targetStatus == TourScheduleStatus.FULL)
                && schedule.getDepartureAt() != null
                && schedule.getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Past schedules cannot be reopened for booking");
        }
    }

    private void validateNonNegativePrice(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(fieldName + " must be greater than or equal to 0");
        }
    }

    private void validateMedia(java.util.List<TourMediaRequest> media) {
        if (media == null) {
            return;
        }
        Set<Integer> sortOrders = new HashSet<>();
        for (TourMediaRequest item : media) {
            Integer sortOrder = item.getSortOrder() == null ? 0 : item.getSortOrder();
            if (!sortOrders.add(sortOrder)) {
                throw new BadRequestException("Media sortOrder must be unique within the tour");
            }
        }
    }

    private void validateItineraryDays(java.util.List<TourItineraryDayRequest> itineraryDays, Integer durationDays) {
        if (itineraryDays == null) {
            return;
        }
        Set<Integer> dayNumbers = new HashSet<>();
        for (TourItineraryDayRequest day : itineraryDays) {
            if (!dayNumbers.add(day.getDayNumber())) {
                throw new BadRequestException("Itinerary dayNumber must be unique within the tour");
            }
            if (durationDays != null && day.getDayNumber() > durationDays) {
                throw new BadRequestException("Itinerary dayNumber must not exceed durationDays");
            }
            validateItineraryItems(day.getItems());
        }
    }

    private void validateItineraryItems(java.util.List<ItineraryItemRequest> items) {
        if (items == null) {
            return;
        }
        Set<Integer> sequenceNumbers = new HashSet<>();
        for (ItineraryItemRequest item : items) {
            if (!sequenceNumbers.add(item.getSequenceNo())) {
                throw new BadRequestException("Itinerary item sequenceNo must be unique within a day");
            }
            if (item.getStartTime() != null && item.getEndTime() != null && item.getEndTime().isBefore(item.getStartTime())) {
                throw new BadRequestException("Itinerary item endTime must not be before startTime");
            }
        }
    }

    private void validateChecklistItems(java.util.List<TourChecklistItemRequest> checklistItems) {
        if (checklistItems == null) {
            return;
        }
        Set<String> normalizedNames = new HashSet<>();
        for (TourChecklistItemRequest item : checklistItems) {
            String key = item.getItemName().trim().toLowerCase(Locale.ROOT);
            if (!normalizedNames.add(key)) {
                throw new BadRequestException("Checklist item names must be unique within the tour");
            }
        }
    }

    private void validateTagIds(java.util.List<Long> tagIds) {
        if (tagIds == null) {
            return;
        }
        Set<Long> uniqueTagIds = new HashSet<>();
        for (Long tagId : tagIds) {
            if (tagId == null || tagId <= 0) {
                throw new BadRequestException("tagIds must contain positive ids only");
            }
            if (!uniqueTagIds.add(tagId)) {
                throw new BadRequestException("tagIds must be unique");
            }
        }
    }

    private void validateSeasonality(java.util.List<TourSeasonalityRequest> seasonalityRequests) {
        if (seasonalityRequests == null) {
            return;
        }
        Set<String> uniqueSeasonNames = new HashSet<>();
        for (TourSeasonalityRequest season : seasonalityRequests) {
            String nameKey = season.getSeasonName().trim().toLowerCase(Locale.ROOT);
            if (!uniqueSeasonNames.add(nameKey)) {
                throw new BadRequestException("Season names must be unique within the tour");
            }
            if (season.getMonthFrom() != null && season.getMonthTo() != null && season.getMonthTo() < season.getMonthFrom()) {
                throw new BadRequestException("seasonality monthTo must not be smaller than monthFrom");
            }
            validateNonNegativePrice(season.getRecommendationScore(), "seasonality recommendationScore");
        }
    }
}
