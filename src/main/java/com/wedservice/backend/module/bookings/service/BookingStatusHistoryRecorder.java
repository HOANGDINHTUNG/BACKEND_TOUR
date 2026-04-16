package com.wedservice.backend.module.bookings.service;

import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatusHistory;
import com.wedservice.backend.module.bookings.repository.BookingStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingStatusHistoryRecorder {

    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;

    public void record(Long bookingId, BookingStatus oldStatus, BookingStatus newStatus, UUID changedBy, String reason) {
        BookingStatusHistory history = BookingStatusHistory.builder()
                .bookingId(bookingId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changeReason(DataNormalizer.normalize(reason))
                .build();

        bookingStatusHistoryRepository.save(history);
    }
}
