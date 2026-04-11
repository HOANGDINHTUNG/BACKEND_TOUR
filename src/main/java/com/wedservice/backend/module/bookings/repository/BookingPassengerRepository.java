package com.wedservice.backend.module.bookings.repository;

import com.wedservice.backend.module.bookings.entity.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, Long> {

    List<BookingPassenger> findByBookingId(Long bookingId);

}
