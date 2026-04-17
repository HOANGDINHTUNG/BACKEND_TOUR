package com.wedservice.backend.module.bookings.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.bookings.dto.request.BookingQuoteRequest;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.CreatePassengerRequest;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.ToIntFunction;

@Component
public class BookingValidator {

    private static final Set<String> ALLOWED_PASSENGER_TYPES = Set.of("adult", "child", "infant", "senior");
    private static final Set<String> ALLOWED_PASSENGER_GENDERS = Set.of("male", "female", "other", "unknown");

    public void validateCreateRequest(CreateBookingRequest request) {
        validateTravellerCounts(request.getAdults(), request.getChildren(), request.getInfants(), request.getSeniors());

        int totalTravellers = request.getAdults() + request.getChildren() + request.getInfants() + request.getSeniors();
        validatePassengerManifest(request, totalTravellers);
    }

    public void validateQuoteRequest(BookingQuoteRequest request) {
        validateTravellerCounts(request.getAdults(), request.getChildren(), request.getInfants(), request.getSeniors());
    }

    public void validateScheduleForBooking(CreateBookingRequest request, TourSchedule schedule, LocalDateTime now) {
        if (!schedule.getTourId().equals(request.getTourId())) {
            throw new BadRequestException("Schedule does not belong to the requested tour");
        }
        if (schedule.getStatus() != TourScheduleStatus.OPEN) {
            throw new BadRequestException("Schedule is not open for booking");
        }
        if (schedule.getBookingOpenAt() != null && now.isBefore(schedule.getBookingOpenAt())) {
            throw new BadRequestException("Booking window has not opened yet");
        }
        if (schedule.getBookingCloseAt() != null && now.isAfter(schedule.getBookingCloseAt())) {
            throw new BadRequestException("Booking window has already closed");
        }

        int requestedSeats = request.getAdults() + request.getChildren() + request.getSeniors();
        int bookedSeats = schedule.getBookedSeats() == null ? 0 : schedule.getBookedSeats();
        int capacityTotal = schedule.getCapacityTotal() == null ? 0 : schedule.getCapacityTotal();

        if (requestedSeats <= 0) {
            throw new BadRequestException("Booking must include at least one seat-taking traveller");
        }
        if (capacityTotal > 0 && bookedSeats + requestedSeats > capacityTotal) {
            throw new BadRequestException("Schedule does not have enough remaining seats");
        }
    }

    public BigDecimal calculateSubtotal(CreateBookingRequest request, TourSchedule schedule) {
        return calculateSubtotal(request.getAdults(), request.getChildren(), request.getInfants(), request.getSeniors(), schedule);
    }

    public BigDecimal calculateSubtotal(BookingQuoteRequest request, TourSchedule schedule) {
        return calculateSubtotal(request.getAdults(), request.getChildren(), request.getInfants(), request.getSeniors(), schedule);
    }

    public BigDecimal calculateSubtotal(int adults, int children, int infants, int seniors, TourSchedule schedule) {
        return multiply(schedule.getAdultPrice(), adults)
                .add(multiply(schedule.getChildPrice(), children))
                .add(multiply(schedule.getInfantPrice(), infants))
                .add(multiply(schedule.getSeniorPrice(), seniors));
    }

    public int calculateSeatCount(int adults, int children, int seniors) {
        return adults + children + seniors;
    }

    public int calculateTravellerCount(int adults, int children, int infants, int seniors) {
        return adults + children + infants + seniors;
    }

    public String normalizePassengerType(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            throw new BadRequestException("passengerType is required for each passenger");
        }
        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_PASSENGER_TYPES.contains(normalized)) {
            throw new BadRequestException("Invalid passengerType: " + rawValue);
        }
        return normalized;
    }

    public String normalizePassengerGender(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return "unknown";
        }
        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_PASSENGER_GENDERS.contains(normalized)) {
            throw new BadRequestException("Invalid passenger gender: " + rawValue);
        }
        return normalized;
    }

    public LocalDate parsePassengerDateOfBirth(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }
        try {
            return LocalDate.parse(rawValue.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("dateOfBirth must use yyyy-MM-dd format");
        }
    }

    private void validatePassengerManifest(CreateBookingRequest request, int totalTravellers) {
        List<CreatePassengerRequest> passengers = request.getPassengers();
        if (passengers == null || passengers.isEmpty()) {
            return;
        }
        if (passengers.size() > totalTravellers) {
            throw new BadRequestException("Passenger list cannot exceed total traveller count");
        }

        validatePassengerTypeCount(passengers, request.getAdults(), "adult");
        validatePassengerTypeCount(passengers, request.getChildren(), "child");
        validatePassengerTypeCount(passengers, request.getInfants(), "infant");
        validatePassengerTypeCount(passengers, request.getSeniors(), "senior");
    }

    private void validateTravellerCounts(int adults, int children, int infants, int seniors) {
        if (children < 0 || infants < 0 || seniors < 0) {
            throw new BadRequestException("Passenger counts cannot be negative");
        }

        int totalTravellers = calculateTravellerCount(adults, children, infants, seniors);
        if (totalTravellers <= 0) {
            throw new BadRequestException("Booking must include at least one traveller");
        }
    }

    private void validatePassengerTypeCount(List<CreatePassengerRequest> passengers, int allowedCount, String passengerType) {
        int actualCount = passengers.stream()
                .filter(passenger -> StringUtils.hasText(passenger.getPassengerType()))
                .map(CreatePassengerRequest::getPassengerType)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(passengerType::equals)
                .mapToInt(alwaysOne())
                .sum();

        if (actualCount > allowedCount) {
            throw new BadRequestException("Passenger list contains too many " + passengerType + " entries");
        }
    }

    private ToIntFunction<String> alwaysOne() {
        return ignored -> 1;
    }

    private BigDecimal multiply(BigDecimal price, int quantity) {
        BigDecimal safePrice = price == null ? BigDecimal.ZERO : price;
        return safePrice.multiply(BigDecimal.valueOf(quantity));
    }
}
