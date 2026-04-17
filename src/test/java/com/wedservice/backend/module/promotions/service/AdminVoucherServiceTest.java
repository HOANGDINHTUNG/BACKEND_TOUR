package com.wedservice.backend.module.promotions.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.promotions.dto.request.VoucherRequest;
import com.wedservice.backend.module.promotions.dto.request.VoucherSearchRequest;
import com.wedservice.backend.module.promotions.dto.response.VoucherResponse;
import com.wedservice.backend.module.promotions.entity.PromotionCampaign;
import com.wedservice.backend.module.promotions.entity.Voucher;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.promotions.repository.PromotionCampaignRepository;
import com.wedservice.backend.module.promotions.repository.VoucherRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminVoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private PromotionCampaignRepository promotionCampaignRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    private AdminVoucherService adminVoucherService;

    @BeforeEach
    void setUp() {
        adminVoucherService = new AdminVoucherService(
                voucherRepository,
                promotionCampaignRepository,
                tourRepository,
                destinationRepository,
                auditTrailRecorder
        );
    }

    @Test
    void createVoucher_normalizesCodeAndRecordsAudit() {
        VoucherRequest request = VoucherRequest.builder()
                .code(" spring10 ")
                .campaignId(3L)
                .name(" Spring 10 ")
                .description(" Discount for spring ")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .maxDiscountAmount(new BigDecimal("500000"))
                .minOrderValue(new BigDecimal("1000000"))
                .usageLimitTotal(100)
                .usageLimitPerUser(2)
                .applicableScope(VoucherApplicableScope.TOUR)
                .applicableTourId(9L)
                .applicableMemberLevel(MemberLevel.GOLD)
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .isStackable(true)
                .isActive(true)
                .build();

        PromotionCampaign campaign = PromotionCampaign.builder()
                .id(3L)
                .startAt(LocalDateTime.of(2026, 4, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 5, 1, 0, 0))
                .build();

        when(voucherRepository.findByCodeIgnoreCase("SPRING10")).thenReturn(Optional.empty());
        when(promotionCampaignRepository.findById(3L)).thenReturn(Optional.of(campaign));
        when(tourRepository.findById(9L)).thenReturn(Optional.of(Tour.builder().id(9L).build()));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> {
            Voucher voucher = invocation.getArgument(0);
            voucher.setId(21L);
            voucher.setCreatedAt(LocalDateTime.now());
            voucher.setUpdatedAt(LocalDateTime.now());
            return voucher;
        });

        VoucherResponse response = adminVoucherService.createVoucher(request);

        assertThat(response.getId()).isEqualTo(21L);
        assertThat(response.getCode()).isEqualTo("SPRING10");
        assertThat(response.getApplicableScope()).isEqualTo(VoucherApplicableScope.TOUR);
        assertThat(response.getApplicableTourId()).isEqualTo(9L);
        assertThat(response.getCampaignId()).isEqualTo(3L);
        verify(auditTrailRecorder).record(
                org.mockito.ArgumentMatchers.eq(AuditActionType.VOUCHER_CREATE),
                org.mockito.ArgumentMatchers.eq(21L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(VoucherResponse.class)
        );
    }

    @Test
    void createVoucher_rejectsPercentageAbove100() {
        VoucherRequest request = VoucherRequest.builder()
                .code("BIG")
                .name("Big")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("120"))
                .minOrderValue(BigDecimal.ZERO)
                .applicableScope(VoucherApplicableScope.ALL)
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .build();

        assertThatThrownBy(() -> adminVoucherService.createVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("discountValue must be <= 100 for percentage vouchers");
    }

    @Test
    void createVoucher_rejectsTourScopeWithoutTourId() {
        VoucherRequest request = VoucherRequest.builder()
                .code("TOUR_ONLY")
                .name("Tour only")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("100000"))
                .minOrderValue(BigDecimal.ZERO)
                .applicableScope(VoucherApplicableScope.TOUR)
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .build();

        assertThatThrownBy(() -> adminVoucherService.createVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("applicableTourId is required when applicableScope is tour");
    }

    @Test
    void getVouchers_returnsPagedResponse() {
        VoucherSearchRequest request = new VoucherSearchRequest();
        Voucher voucher = Voucher.builder()
                .id(8L)
                .code("MAY100")
                .name("May 100")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("100000"))
                .minOrderValue(BigDecimal.ZERO)
                .usageLimitPerUser(1)
                .usedCount(0)
                .applicableScope(VoucherApplicableScope.ALL)
                .startAt(LocalDateTime.of(2026, 5, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 5, 31, 23, 59))
                .isActive(true)
                .build();

        when(voucherRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(voucher), PageRequest.of(0, 10), 1));

        PageResponse<VoucherResponse> response = adminVoucherService.getVouchers(request);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCode()).isEqualTo("MAY100");
    }

    @Test
    void updateVoucherStatus_updatesFlagAndRecordsAudit() {
        Voucher voucher = Voucher.builder()
                .id(7L)
                .code("FLASH")
                .name("Flash")
                .discountType(VoucherDiscountType.CASHBACK)
                .discountValue(new BigDecimal("50000"))
                .minOrderValue(BigDecimal.ZERO)
                .usageLimitPerUser(1)
                .usedCount(0)
                .applicableScope(VoucherApplicableScope.ALL)
                .startAt(LocalDateTime.of(2026, 5, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 5, 10, 0, 0))
                .isActive(true)
                .build();

        when(voucherRepository.findById(7L)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VoucherResponse response = adminVoucherService.updateVoucherStatus(7L, false);

        assertThat(response.getIsActive()).isFalse();
        verify(auditTrailRecorder).record(
                org.mockito.ArgumentMatchers.eq(AuditActionType.VOUCHER_STATUS_UPDATE),
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.any(VoucherResponse.class),
                org.mockito.ArgumentMatchers.any(VoucherResponse.class)
        );
    }
}
