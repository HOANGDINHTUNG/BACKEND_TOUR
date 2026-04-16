package com.wedservice.backend.module.bookings.repository;

import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select coalesce(sum(b.adults + b.children + b.seniors), 0)
            from Booking b
            where b.scheduleId = :scheduleId
              and b.status in :statuses
            """)
    Long sumSeatOccupancyByScheduleIdAndStatusIn(
            @Param("scheduleId") Long scheduleId,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    long countByTourIdAndStatusIn(Long tourId, Collection<BookingStatus> statuses);
}
