package com.wedservice.backend.module.bookings.service.command.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.service.BookingStatusHistoryRecorder;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.CreatePassengerRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingPassenger;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingPassengerRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.service.command.BookingCommandService;
import com.wedservice.backend.module.bookings.validator.BookingValidator;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository passengerRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final BookingValidator bookingValidator;
    private final BookingStatusHistoryRecorder bookingStatusHistoryRecorder;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        bookingValidator.validateCreateRequest(request);
        UUID ownerId = resolveBookingOwnerId(request.getUserId());
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new BadRequestException("Schedule not found"));
        bookingValidator.validateScheduleForBooking(request, schedule, LocalDateTime.now());
        BigDecimal subtotalAmount = bookingValidator.calculateSubtotal(request, schedule);

        Booking booking = Booking.builder()
                .bookingCode("BK" + System.currentTimeMillis())
                .userId(ownerId)
                .tourId(request.getTourId())
                .scheduleId(request.getScheduleId())
                .contactName(DataNormalizer.normalize(request.getContactName()))
                .contactPhone(DataNormalizer.normalizePhone(request.getContactPhone()))
                .contactEmail(DataNormalizer.normalizeEmail(request.getContactEmail()))
                .adults(request.getAdults())
                .children(request.getChildren())
                .infants(request.getInfants())
                .seniors(request.getSeniors())
                .status(BookingStatus.PENDING_PAYMENT)
                .paymentStatus(BookingPaymentStatus.UNPAID)
                .subtotalAmount(subtotalAmount)
                .finalAmount(subtotalAmount)
                .currency("VND")
                .build();

        booking = bookingRepository.save(booking);
        bookingStatusHistoryRecorder.record(
                booking.getId(),
                null,
                booking.getStatus(),
                authenticatedUserProvider.getRequiredCurrentUserId(),
                "Booking created"
        );

        if (request.getPassengers() != null) {
            for (CreatePassengerRequest p : request.getPassengers()) {
                BookingPassenger bp = BookingPassenger.builder()
                        .bookingId(booking.getId())
                        .fullName(DataNormalizer.normalize(p.getFullName()))
                        .passengerType(bookingValidator.normalizePassengerType(p.getPassengerType()))
                        .gender(bookingValidator.normalizePassengerGender(p.getGender()))
                        .dateOfBirth(bookingValidator.parsePassengerDateOfBirth(p.getDateOfBirth()))
                        .identityNo(DataNormalizer.normalize(p.getIdentityNo()))
                        .passportNo(DataNormalizer.normalize(p.getPassportNo()))
                        .phone(DataNormalizer.normalizePhone(p.getPhone()))
                        .email(DataNormalizer.normalizeEmail(p.getEmail()))
                        .build();

                passengerRepository.save(bp);
            }
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus().getValue())
                .finalAmount(booking.getFinalAmount())
                .build();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, String reason) {
        Booking booking = findAccessibleBooking(id);
        BookingStatus targetStatus = determineCancellationStatus(booking);
        return updateBookingStatus(booking, targetStatus, reason);
    }

    @Override
    @Transactional
    public BookingResponse checkInBooking(Long id, String reason) {
        Booking booking = findAccessibleBooking(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed bookings can be checked in");
        }
        if (booking.getPaymentStatus() != BookingPaymentStatus.PAID) {
            throw new BadRequestException("Only paid bookings can be checked in");
        }
        return updateBookingStatus(booking, BookingStatus.CHECKED_IN, reason);
    }

    @Override
    @Transactional
    public BookingResponse completeBooking(Long id, String reason) {
        Booking booking = findAccessibleBooking(id);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new BadRequestException("Only checked-in bookings can be completed");
        }
        return updateBookingStatus(booking, BookingStatus.COMPLETED, reason);
    }

    private UUID resolveBookingOwnerId(String requestedUserId) {
        UUID currentUserId = authenticatedUserProvider.getRequiredCurrentUserId();
        if (!authenticatedUserProvider.isCurrentUserBackoffice()) {
            return currentUserId;
        }
        if (!StringUtils.hasText(requestedUserId)) {
            return currentUserId;
        }
        try {
            return UUID.fromString(requestedUserId);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("userId must be a valid UUID");
        }
    }

    private Booking findAccessibleBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (authenticatedUserProvider.isCurrentUserBackoffice()) {
            return booking;
        }
        if (!authenticatedUserProvider.getRequiredCurrentUserId().equals(booking.getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this booking");
        }
        return booking;
    }

    private BookingStatus determineCancellationStatus(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT || booking.getStatus() == BookingStatus.CONFIRMED) {
            if (booking.getPaymentStatus() == BookingPaymentStatus.PAID) {
                return BookingStatus.CANCEL_REQUESTED;
            }
            return BookingStatus.CANCELLED;
        }
        throw new BadRequestException("Booking cannot be cancelled from the current status");
    }

    private BookingResponse updateBookingStatus(Booking booking, BookingStatus newStatus, String reason) {
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(newStatus);
        bookingRepository.save(booking);
        bookingStatusHistoryRecorder.record(
                booking.getId(),
                oldStatus,
                newStatus,
                authenticatedUserProvider.getRequiredCurrentUserId(),
                reason
        );
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus().getValue())
                .finalAmount(booking.getFinalAmount())
                .build();
    }
}
