package com.wedservice.backend.module.bookings.service.command.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.service.BookingStatusHistoryRecorder;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.dto.request.BookingQuoteRequest;
import com.wedservice.backend.module.bookings.dto.request.CreatePassengerRequest;
import com.wedservice.backend.module.bookings.dto.response.BookingQuoteResponse;
import com.wedservice.backend.module.bookings.dto.response.BookingResponse;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingComboItem;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingPassenger;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingComboItemRepository;
import com.wedservice.backend.module.bookings.repository.BookingPassengerRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.service.BookingPricingService;
import com.wedservice.backend.module.bookings.service.command.BookingCommandService;
import com.wedservice.backend.module.bookings.validator.BookingValidator;
import com.wedservice.backend.module.loyalty.service.MissionTrackerService;
import com.wedservice.backend.module.loyalty.service.UserPassportService;
import com.wedservice.backend.module.tours.service.TourRuntimeStatsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingRepository bookingRepository;
    private final BookingComboItemRepository bookingComboItemRepository;
    private final BookingPassengerRepository passengerRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final BookingValidator bookingValidator;
    private final BookingPricingService bookingPricingService;
    private final BookingStatusHistoryRecorder bookingStatusHistoryRecorder;
    private final TourRuntimeStatsSyncService tourRuntimeStatsSyncService;
    private final UserPassportService userPassportService;
    private final MissionTrackerService missionTrackerService;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        bookingValidator.validateCreateRequest(request);
        UUID ownerId = resolveBookingOwnerId(request.getUserId());
        BookingQuoteResponse quote = bookingPricingService.quoteBookingForUser(ownerId, toQuoteRequest(request));

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
                .subtotalAmount(quote.getSubtotalAmount())
                .discountAmount(quote.getDiscountAmount())
                .voucherDiscountAmount(quote.getVoucherDiscountAmount())
                .loyaltyDiscountAmount(quote.getLoyaltyDiscountAmount())
                .addonAmount(quote.getAddonAmount())
                .taxAmount(quote.getTaxAmount())
                .finalAmount(quote.getFinalAmount())
                .currency(quote.getCurrency())
                .voucherId(quote.getAppliedVoucher() == null ? null : quote.getAppliedVoucher().getVoucherId())
                .comboId(quote.getAppliedCombo() == null ? null : quote.getAppliedCombo().getComboId())
                .build();

        booking = bookingRepository.save(booking);
        saveComboSnapshotIfPresent(booking, quote);
        tourRuntimeStatsSyncService.syncScheduleState(booking.getScheduleId());
        tourRuntimeStatsSyncService.syncTourBookingStats(booking.getTourId());
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
                .subtotalAmount(booking.getSubtotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .voucherDiscountAmount(booking.getVoucherDiscountAmount())
                .addonAmount(booking.getAddonAmount())
                .finalAmount(booking.getFinalAmount())
                .voucherId(booking.getVoucherId())
                .comboId(booking.getComboId())
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
        BookingResponse response = updateBookingStatus(booking, BookingStatus.CHECKED_IN, reason);
        userPassportService.recordBookingCheckIn(booking, reason);
        return response;
    }

    @Override
    @Transactional
    public BookingResponse completeBooking(Long id, String reason) {
        Booking booking = findAccessibleBooking(id);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new BadRequestException("Only checked-in bookings can be completed");
        }
        BookingResponse response = updateBookingStatus(booking, BookingStatus.COMPLETED, reason);
        missionTrackerService.incrementProgress(booking.getUserId(), "TOTAL_BOOKINGS", java.math.BigDecimal.ONE);
        return response;
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
        tourRuntimeStatsSyncService.syncScheduleState(booking.getScheduleId());
        tourRuntimeStatsSyncService.syncTourBookingStats(booking.getTourId());
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
                .subtotalAmount(booking.getSubtotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .voucherDiscountAmount(booking.getVoucherDiscountAmount())
                .addonAmount(booking.getAddonAmount())
                .finalAmount(booking.getFinalAmount())
                .voucherId(booking.getVoucherId())
                .comboId(booking.getComboId())
                .build();
    }

    private void saveComboSnapshotIfPresent(Booking booking, BookingQuoteResponse quote) {
        if (quote.getAppliedCombo() == null) {
            return;
        }

        bookingComboItemRepository.save(BookingComboItem.builder()
                .bookingId(booking.getId())
                .comboId(quote.getAppliedCombo().getComboId())
                .unitPrice(quote.getAppliedCombo().getUnitPrice())
                .discountAmount(quote.getAppliedCombo().getDiscountAmount())
                .finalPrice(quote.getAppliedCombo().getFinalPrice())
                .build());
    }

    private BookingQuoteRequest toQuoteRequest(CreateBookingRequest request) {
        return BookingQuoteRequest.builder()
                .tourId(request.getTourId())
                .scheduleId(request.getScheduleId())
                .adults(request.getAdults())
                .children(request.getChildren())
                .infants(request.getInfants())
                .seniors(request.getSeniors())
                .voucherCode(request.getVoucherCode())
                .comboId(request.getComboId())
                .build();
    }
}
