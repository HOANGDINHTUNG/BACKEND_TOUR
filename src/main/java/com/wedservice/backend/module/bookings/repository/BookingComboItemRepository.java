package com.wedservice.backend.module.bookings.repository;

import com.wedservice.backend.module.bookings.entity.BookingComboItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingComboItemRepository extends JpaRepository<BookingComboItem, Long> {
}
