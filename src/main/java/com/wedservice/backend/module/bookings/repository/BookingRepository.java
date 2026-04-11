package com.wedservice.backend.module.bookings.repository;

import com.wedservice.backend.module.bookings.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
