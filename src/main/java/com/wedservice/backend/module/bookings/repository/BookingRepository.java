package com.wedservice.backend.module.bookings.repository;

import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    boolean existsByScheduleIdAndUserIdAndStatusIn(
            Long scheduleId,
            UUID userId,
            Collection<BookingStatus> statuses
    );

    @Query("""
            select distinct b.userId
            from Booking b
            where b.scheduleId = :scheduleId
              and b.status in :statuses
            """)
    List<UUID> findDistinctUserIdsByScheduleIdAndStatusIn(
            @Param("scheduleId") Long scheduleId,
            @Param("statuses") Collection<BookingStatus> statuses
    );
}
