package com.wedservice.backend.module.bookings.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.bookings.dto.request.BookingQuoteRequest;
import com.wedservice.backend.module.bookings.dto.response.AppliedComboQuoteResponse;
import com.wedservice.backend.module.bookings.dto.response.AppliedVoucherQuoteResponse;
import com.wedservice.backend.module.bookings.dto.response.BookingQuoteResponse;
import com.wedservice.backend.module.bookings.validator.BookingValidator;
import com.wedservice.backend.module.commerce.entity.ComboPackage;
import com.wedservice.backend.module.commerce.repository.ComboPackageRepository;
import com.wedservice.backend.module.promotions.entity.Voucher;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.promotions.entity.VoucherUserClaim;
import com.wedservice.backend.module.promotions.repository.VoucherRepository;
import com.wedservice.backend.module.promotions.repository.VoucherUserClaimRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingPricingService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final TourScheduleRepository tourScheduleRepository;
    private final TourRepository tourRepository;
    private final ComboPackageRepository comboPackageRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUserClaimRepository voucherUserClaimRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final BookingValidator bookingValidator;

    @Transactional(readOnly = true)
    public BookingQuoteResponse quoteBooking(BookingQuoteRequest request) {
        return quoteBookingForUser(authenticatedUserProvider.getRequiredCurrentUserId(), request);
    }

    @Transactional(readOnly = true)
    public BookingQuoteResponse quoteBookingForUser(UUID userId, BookingQuoteRequest request) {
        bookingValidator.validateQuoteRequest(request);

        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new BadRequestException("Schedule not found"));
        bookingValidator.validateScheduleForBooking(toCreateLikeRequest(request), schedule, LocalDateTime.now());
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + request.getTourId()));

        BigDecimal subtotalAmount = bookingValidator.calculateSubtotal(request, schedule);
        ComboQuote comboQuote = resolveComboQuote(request.getComboId());
        VoucherQuote voucherQuote = resolveVoucherQuote(request.getVoucherCode(), userId, tour, subtotalAmount);
        BigDecimal finalAmount = subtotalAmount
                .subtract(voucherQuote.voucherDiscountAmount())
                .add(comboQuote.finalPrice());

        return BookingQuoteResponse.builder()
                .tourId(request.getTourId())
                .scheduleId(request.getScheduleId())
                .adults(request.getAdults())
                .children(request.getChildren())
                .infants(request.getInfants())
                .seniors(request.getSeniors())
                .seatCount(bookingValidator.calculateSeatCount(request.getAdults(), request.getChildren(), request.getSeniors()))
                .travellerCount(bookingValidator.calculateTravellerCount(
                        request.getAdults(),
                        request.getChildren(),
                        request.getInfants(),
                        request.getSeniors()
                ))
                .subtotalAmount(subtotalAmount)
                .discountAmount(voucherQuote.voucherDiscountAmount().add(comboQuote.discountAmount()))
                .voucherDiscountAmount(voucherQuote.voucherDiscountAmount())
                .loyaltyDiscountAmount(ZERO)
                .addonAmount(comboQuote.finalPrice())
                .taxAmount(ZERO)
                .finalAmount(finalAmount.max(ZERO))
                .currency("VND")
                .appliedVoucher(voucherQuote.appliedVoucher())
                .appliedCombo(comboQuote.appliedCombo())
                .build();
    }

    private ComboQuote resolveComboQuote(Long comboId) {
        if (comboId == null) {
            return new ComboQuote(ZERO, ZERO, null);
        }

        ComboPackage comboPackage = comboPackageRepository.findDetailById(comboId)
                .orElseThrow(() -> new ResourceNotFoundException("Combo package not found with id: " + comboId));

        if (!Boolean.TRUE.equals(comboPackage.getIsActive())) {
            throw new BadRequestException("Combo package is inactive");
        }

        BigDecimal finalPrice = comboPackage.getBasePrice().subtract(comboPackage.getDiscountAmount()).max(ZERO);
        AppliedComboQuoteResponse appliedCombo = AppliedComboQuoteResponse.builder()
                .comboId(comboPackage.getId())
                .comboCode(comboPackage.getCode())
                .comboName(comboPackage.getName())
                .unitPrice(comboPackage.getBasePrice())
                .discountAmount(comboPackage.getDiscountAmount())
                .finalPrice(finalPrice)
                .build();

        return new ComboQuote(comboPackage.getDiscountAmount(), finalPrice, appliedCombo);
    }

    private VoucherQuote resolveVoucherQuote(String rawVoucherCode, UUID userId, Tour tour, BigDecimal subtotalAmount) {
        if (!StringUtils.hasText(rawVoucherCode)) {
            return new VoucherQuote(ZERO, null);
        }

        User user = findActiveUser(userId);
        String voucherCode = normalizeVoucherCode(rawVoucherCode);
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(voucherCode)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + voucherCode));

        VoucherUserClaim claim = voucherUserClaimRepository.findByVoucherIdAndUserId(voucher.getId(), user.getId())
                .orElseThrow(() -> new BadRequestException("Voucher must be claimed before it can be applied"));

        validateVoucherForQuote(voucher, claim, user, tour, subtotalAmount);

        BigDecimal discountAmount = calculateVoucherDiscount(voucher, subtotalAmount);
        AppliedVoucherQuoteResponse appliedVoucher = AppliedVoucherQuoteResponse.builder()
                .claimId(claim.getId())
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .voucherName(voucher.getName())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .build();

        return new VoucherQuote(discountAmount, appliedVoucher);
    }

    private void validateVoucherForQuote(Voucher voucher, VoucherUserClaim claim, User user, Tour tour, BigDecimal subtotalAmount) {
        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(voucher.getIsActive())) {
            throw new BadRequestException("Voucher is inactive");
        }
        if (now.isBefore(voucher.getStartAt()) || now.isAfter(voucher.getEndAt())) {
            throw new BadRequestException("Voucher is not applicable at this time");
        }
        if (voucher.getApplicableMemberLevel() != null && voucher.getApplicableMemberLevel() != user.getMemberLevel()) {
            throw new BadRequestException("Voucher is not available for your member level");
        }
        if (voucher.getUsageLimitTotal() != null && safeInteger(voucher.getUsedCount()) >= voucher.getUsageLimitTotal()) {
            throw new BadRequestException("Voucher has reached total usage limit");
        }
        if (voucher.getUsageLimitPerUser() != null && safeInteger(claim.getUsedCount()) >= voucher.getUsageLimitPerUser()) {
            throw new BadRequestException("Voucher has reached per-user usage limit");
        }
        if (voucher.getMinOrderValue() != null && subtotalAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BadRequestException("Subtotal does not meet voucher minimum order value");
        }
        if (voucher.getDiscountType() != VoucherDiscountType.PERCENTAGE && voucher.getDiscountType() != VoucherDiscountType.FIXED_AMOUNT) {
            throw new BadRequestException("Only percentage and fixed_amount vouchers are supported in booking pricing");
        }

        if (voucher.getApplicableScope() == VoucherApplicableScope.TOUR) {
            if (!tour.getId().equals(voucher.getApplicableTourId())) {
                throw new BadRequestException("Voucher does not apply to the selected tour");
            }
        } else if (voucher.getApplicableScope() == VoucherApplicableScope.DESTINATION) {
            Long destinationId = tour.getDestination() == null ? null : tour.getDestination().getId();
            if (destinationId == null || !destinationId.equals(voucher.getApplicableDestinationId())) {
                throw new BadRequestException("Voucher does not apply to the selected destination");
            }
        }
    }

    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal subtotalAmount) {
        BigDecimal discountAmount;

        if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
            discountAmount = subtotalAmount
                    .multiply(voucher.getDiscountValue())
                    .divide(ONE_HUNDRED, 2, java.math.RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else {
            discountAmount = voucher.getDiscountValue();
        }

        if (discountAmount.compareTo(subtotalAmount) > 0) {
            return subtotalAmount;
        }

        return discountAmount.max(ZERO);
    }

    private User findActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() != Status.ACTIVE) {
            throw new UnauthorizedException("Your account is " + user.getStatus().getValue() + ". Please contact support.");
        }

        return user;
    }

    private String normalizeVoucherCode(String rawValue) {
        String normalized = DataNormalizer.normalize(rawValue);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException("voucherCode is required");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest toCreateLikeRequest(BookingQuoteRequest request) {
        return com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest.builder()
                .tourId(request.getTourId())
                .scheduleId(request.getScheduleId())
                .contactName("quote")
                .contactPhone("0000000000")
                .adults(request.getAdults())
                .children(request.getChildren())
                .infants(request.getInfants())
                .seniors(request.getSeniors())
                .comboId(request.getComboId())
                .build();
    }

    private record VoucherQuote(BigDecimal voucherDiscountAmount, AppliedVoucherQuoteResponse appliedVoucher) {
    }

    private record ComboQuote(BigDecimal discountAmount, BigDecimal finalPrice, AppliedComboQuoteResponse appliedCombo) {
    }
}
