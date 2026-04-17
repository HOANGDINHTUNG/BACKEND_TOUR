package com.wedservice.backend.module.promotions.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.promotions.dto.request.ClaimVoucherRequest;
import com.wedservice.backend.module.promotions.dto.response.ClaimedVoucherResponse;
import com.wedservice.backend.module.promotions.entity.ClaimedVoucherStatus;
import com.wedservice.backend.module.promotions.entity.Voucher;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.promotions.entity.VoucherUserClaim;
import com.wedservice.backend.module.promotions.repository.VoucherRepository;
import com.wedservice.backend.module.promotions.repository.VoucherUserClaimRepository;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserVoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherUserClaimRepository voucherUserClaimRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UserVoucherService userVoucherService;

    @BeforeEach
    void setUp() {
        userVoucherService = new UserVoucherService(
                voucherRepository,
                voucherUserClaimRepository,
                userRepository,
                authenticatedUserProvider
        );
    }

    @Test
    void claimVoucher_createsClaimForEligibleUser() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, MemberLevel.GOLD);
        Voucher voucher = activeVoucher(15L, "SPRING10");
        voucher.setApplicableMemberLevel(MemberLevel.GOLD);

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherRepository.findByCodeIgnoreCase("SPRING10")).thenReturn(Optional.of(voucher));
        when(voucherUserClaimRepository.findByVoucherIdAndUserId(15L, userId)).thenReturn(Optional.empty());
        when(voucherUserClaimRepository.save(any(VoucherUserClaim.class))).thenAnswer(invocation -> {
            VoucherUserClaim claim = invocation.getArgument(0);
            claim.setId(9L);
            claim.setClaimedAt(LocalDateTime.now());
            return claim;
        });

        ClaimedVoucherResponse response = userVoucherService.claimVoucher(ClaimVoucherRequest.builder()
                .voucherCode(" spring10 ")
                .build());

        assertThat(response.getClaimId()).isEqualTo(9L);
        assertThat(response.getVoucherCode()).isEqualTo("SPRING10");
        assertThat(response.getStatus()).isEqualTo(ClaimedVoucherStatus.AVAILABLE);
    }

    @Test
    void claimVoucher_rejectsDuplicateClaim() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, MemberLevel.GOLD);
        Voucher voucher = activeVoucher(15L, "SPRING10");

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherRepository.findByCodeIgnoreCase("SPRING10")).thenReturn(Optional.of(voucher));
        when(voucherUserClaimRepository.findByVoucherIdAndUserId(15L, userId))
                .thenReturn(Optional.of(VoucherUserClaim.builder().id(3L).voucherId(15L).userId(userId).build()));

        assertThatThrownBy(() -> userVoucherService.claimVoucher(ClaimVoucherRequest.builder()
                .voucherCode("SPRING10")
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Voucher already claimed");
    }

    @Test
    void claimVoucher_rejectsMemberLevelMismatch() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, MemberLevel.SILVER);
        Voucher voucher = activeVoucher(15L, "SPRING10");
        voucher.setApplicableMemberLevel(MemberLevel.GOLD);

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherRepository.findByCodeIgnoreCase("SPRING10")).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> userVoucherService.claimVoucher(ClaimVoucherRequest.builder()
                .voucherCode("SPRING10")
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Voucher is not available for your member level");
    }

    @Test
    void getMyVouchers_returnsClaimedVoucherListWithComputedStatus() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, MemberLevel.GOLD);
        Voucher voucher = activeVoucher(15L, "SPRING10");
        voucher.setUsageLimitPerUser(2);
        VoucherUserClaim claim = VoucherUserClaim.builder()
                .id(8L)
                .voucherId(15L)
                .userId(userId)
                .claimedAt(LocalDateTime.now().minusDays(1))
                .usedCount(1)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherUserClaimRepository.findByUserIdOrderByClaimedAtDescIdDesc(userId)).thenReturn(List.of(claim));
        when(voucherRepository.findById(15L)).thenReturn(Optional.of(voucher));

        List<ClaimedVoucherResponse> responses = userVoucherService.getMyVouchers();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRemainingUserUses()).isEqualTo(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(ClaimedVoucherStatus.AVAILABLE);
    }

    private User activeUser(UUID userId, MemberLevel memberLevel) {
        return User.builder()
                .id(userId)
                .status(Status.ACTIVE)
                .memberLevel(memberLevel)
                .email("user@example.com")
                .phone("0901000000")
                .passwordHash("encoded")
                .fullName("User")
                .build();
    }

    private Voucher activeVoucher(Long voucherId, String code) {
        return Voucher.builder()
                .id(voucherId)
                .code(code)
                .name("Spring 10")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .minOrderValue(BigDecimal.ZERO)
                .usageLimitPerUser(1)
                .usedCount(0)
                .applicableScope(VoucherApplicableScope.ALL)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(5))
                .isActive(true)
                .build();
    }
}
