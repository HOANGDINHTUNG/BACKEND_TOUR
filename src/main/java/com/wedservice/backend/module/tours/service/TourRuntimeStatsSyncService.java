package com.wedservice.backend.module.tours.service;

import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.reviews.repository.ReviewRepository;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TourRuntimeStatsSyncService {

    private static final Set<BookingStatus> SCHEDULE_OCCUPYING_STATUSES = Set.of(
            BookingStatus.PENDING_PAYMENT,
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN,
            BookingStatus.COMPLETED
    );

    private static final Set<BookingStatus> TOUR_BOOKING_COUNTED_STATUSES = Set.of(
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN,
            BookingStatus.COMPLETED
    );

    private final TourScheduleRepository tourScheduleRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public void syncScheduleState(Long scheduleId) {
        if (scheduleId == null) {
            return;
        }
        tourScheduleRepository.findById(scheduleId).ifPresent(schedule -> {
            int bookedSeats = Math.toIntExact(
                    bookingRepository.sumSeatOccupancyByScheduleIdAndStatusIn(scheduleId, SCHEDULE_OCCUPYING_STATUSES)
            );
            schedule.setBookedSeats(bookedSeats);
            applyCapacityDrivenStatus(schedule, bookedSeats);
            tourScheduleRepository.save(schedule);
        });
    }

    public void syncTourBookingStats(Long tourId) {
        if (tourId == null) {
            return;
        }
        tourRepository.findById(tourId).ifPresent(tour -> {
            tour.setTotalBookings(Math.toIntExact(
                    bookingRepository.countByTourIdAndStatusIn(tourId, TOUR_BOOKING_COUNTED_STATUSES)
            ));
            tourRepository.save(tour);
        });
    }

    public void syncTourRating(Long tourId) {
        if (tourId == null) {
            return;
        }
        tourRepository.findById(tourId).ifPresent(tour -> {
            tour.setTotalReviews(Math.toIntExact(reviewRepository.countByTourId(tourId)));
            tour.setAverageRating(toRatingValue(reviewRepository.findAverageRatingByTourId(tourId)));
            tourRepository.save(tour);
        });
    }

    private void applyCapacityDrivenStatus(TourSchedule schedule, int bookedSeats) {
        Integer capacityTotal = schedule.getCapacityTotal();
        if (capacityTotal == null || capacityTotal <= 0 || schedule.getStatus() == null) {
            return;
        }
        if (schedule.getStatus() == TourScheduleStatus.OPEN && bookedSeats >= capacityTotal) {
            schedule.setStatus(TourScheduleStatus.FULL);
            return;
        }
        if (schedule.getStatus() == TourScheduleStatus.FULL
                && bookedSeats < capacityTotal
                && schedule.getDepartureAt() != null
                && schedule.getDepartureAt().isAfter(LocalDateTime.now())) {
            schedule.setStatus(TourScheduleStatus.OPEN);
        }
    }

    private BigDecimal toRatingValue(Double averageRating) {
        if (averageRating == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);
    }
}
