package com.wedservice.backend.module.promotions.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.promotions.dto.request.ClaimVoucherRequest;
import com.wedservice.backend.module.promotions.dto.response.ClaimedVoucherResponse;
import com.wedservice.backend.module.promotions.entity.ClaimedVoucherStatus;
import com.wedservice.backend.module.promotions.entity.Voucher;
import com.wedservice.backend.module.promotions.entity.VoucherUserClaim;
import com.wedservice.backend.module.promotions.repository.VoucherRepository;
import com.wedservice.backend.module.promotions.repository.VoucherUserClaimRepository;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserVoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUserClaimRepository voucherUserClaimRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<ClaimedVoucherResponse> getMyVouchers() {
        UUID userId = findCurrentUser().getId();

        return voucherUserClaimRepository.findByUserIdOrderByClaimedAtDescIdDesc(userId)
                .stream()
                .map(claim -> toClaimedVoucherResponse(claim, findVoucher(claim.getVoucherId())))
                .toList();
    }

    @Transactional
    public ClaimedVoucherResponse claimVoucher(ClaimVoucherRequest request) {
        User currentUser = findCurrentUser();
        Voucher voucher = findVoucherByCode(request.getVoucherCode());
        validateClaimable(voucher, currentUser);

        voucherUserClaimRepository.findByVoucherIdAndUserId(voucher.getId(), currentUser.getId())
                .ifPresent(existingClaim -> {
                    throw new BadRequestException("Voucher already claimed");
                });

        VoucherUserClaim claim = VoucherUserClaim.builder()
                .voucherId(voucher.getId())
                .userId(currentUser.getId())
                .usedCount(0)
                .build();

        VoucherUserClaim savedClaim = voucherUserClaimRepository.save(claim);
        return toClaimedVoucherResponse(savedClaim, voucher);
    }

    private User findCurrentUser() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() != Status.ACTIVE) {
            throw new UnauthorizedException("Your account is " + user.getStatus().getValue() + ". Please contact support.");
        }

        return user;
    }

    private Voucher findVoucherByCode(String voucherCode) {
        String normalizedCode = normalizeRequiredCode(voucherCode);
        return voucherRepository.findByCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + normalizedCode));
    }

    private Voucher findVoucher(Long voucherId) {
        return voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + voucherId));
    }

    private void validateClaimable(Voucher voucher, User user) {
        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(voucher.getIsActive())) {
            throw new BadRequestException("Voucher is inactive");
        }

        if (now.isBefore(voucher.getStartAt()) || now.isAfter(voucher.getEndAt())) {
            throw new BadRequestException("Voucher is not claimable at this time");
        }

        if (voucher.getApplicableMemberLevel() != null && voucher.getApplicableMemberLevel() != user.getMemberLevel()) {
            throw new BadRequestException("Voucher is not available for your member level");
        }

        if (voucher.getUsageLimitTotal() != null && safeInteger(voucher.getUsedCount()) >= voucher.getUsageLimitTotal()) {
            throw new BadRequestException("Voucher has reached total usage limit");
        }
    }

    private ClaimedVoucherResponse toClaimedVoucherResponse(VoucherUserClaim claim, Voucher voucher) {
        int remainingUserUses = voucher.getUsageLimitPerUser() == null
                ? Integer.MAX_VALUE
                : Math.max(0, voucher.getUsageLimitPerUser() - safeInteger(claim.getUsedCount()));

        return ClaimedVoucherResponse.builder()
                .claimId(claim.getId())
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .voucherName(voucher.getName())
                .voucherDescription(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderValue(voucher.getMinOrderValue())
                .applicableScope(voucher.getApplicableScope())
                .applicableTourId(voucher.getApplicableTourId())
                .applicableDestinationId(voucher.getApplicableDestinationId())
                .applicableMemberLevel(voucher.getApplicableMemberLevel())
                .usageLimitTotal(voucher.getUsageLimitTotal())
                .usageLimitPerUser(voucher.getUsageLimitPerUser())
                .totalUsedCount(voucher.getUsedCount())
                .userUsedCount(claim.getUsedCount())
                .remainingUserUses(voucher.getUsageLimitPerUser() == null ? null : remainingUserUses)
                .startAt(voucher.getStartAt())
                .endAt(voucher.getEndAt())
                .isStackable(voucher.getIsStackable())
                .isActive(voucher.getIsActive())
                .claimedAt(claim.getClaimedAt())
                .status(resolveStatus(voucher, claim))
                .build();
    }

    private ClaimedVoucherStatus resolveStatus(Voucher voucher, VoucherUserClaim claim) {
        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(voucher.getIsActive())) {
            return ClaimedVoucherStatus.INACTIVE;
        }

        if (now.isAfter(voucher.getEndAt())) {
            return ClaimedVoucherStatus.EXPIRED;
        }

        if (voucher.getUsageLimitTotal() != null && safeInteger(voucher.getUsedCount()) >= voucher.getUsageLimitTotal()) {
            return ClaimedVoucherStatus.EXHAUSTED_TOTAL;
        }

        if (voucher.getUsageLimitPerUser() != null && safeInteger(claim.getUsedCount()) >= voucher.getUsageLimitPerUser()) {
            return ClaimedVoucherStatus.USED_UP;
        }

        return ClaimedVoucherStatus.AVAILABLE;
    }

    private String normalizeRequiredCode(String rawValue) {
        String normalized = DataNormalizer.normalize(rawValue);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException("voucherCode is required");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }
}
