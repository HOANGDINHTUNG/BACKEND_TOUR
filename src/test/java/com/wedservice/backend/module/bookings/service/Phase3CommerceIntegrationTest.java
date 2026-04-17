package com.wedservice.backend.module.bookings.service;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingComboItem;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingComboItemRepository;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.service.command.BookingCommandService;
import com.wedservice.backend.module.commerce.entity.ComboPackage;
import com.wedservice.backend.module.commerce.entity.ComboPackageItem;
import com.wedservice.backend.module.commerce.repository.ComboPackageRepository;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.service.command.PaymentCommandService;
import com.wedservice.backend.module.promotions.entity.Voucher;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.promotions.entity.VoucherUserClaim;
import com.wedservice.backend.module.promotions.repository.VoucherRepository;
import com.wedservice.backend.module.promotions.repository.VoucherUserClaimRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Phase3CommerceIntegrationTest {

    @Autowired
    private BookingCommandService bookingCommandService;

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingComboItemRepository bookingComboItemRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherUserClaimRepository voucherUserClaimRepository;

    @Autowired
    private ComboPackageRepository comboPackageRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        userRepository.save(User.builder()
                .id(currentUserId)
                .fullName("Phase 3 User")
                .email("phase3@example.com")
                .phone("0909333444")
                .passwordHash("encoded-password")
                .status(Status.ACTIVE)
                .memberLevel(MemberLevel.GOLD)
                .build());

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
    }

    @Test
    void bookingWithVoucherAndCombo_persistsSnapshotAndSyncsVoucherUsageOnPayment() {
        Tour tour = seedActiveTour();
        TourSchedule schedule = seedOpenSchedule(tour.getId());
        ComboPackage comboPackage = seedActiveComboPackage();
        Voucher voucher = seedVoucher(tour.getId());
        VoucherUserClaim claim = voucherUserClaimRepository.save(VoucherUserClaim.builder()
                .voucherId(voucher.getId())
                .userId(currentUserId)
                .usedCount(0)
                .build());

        var bookingResponse = bookingCommandService.createBooking(CreateBookingRequest.builder()
                .userId(currentUserId.toString())
                .tourId(tour.getId())
                .scheduleId(schedule.getId())
                .contactName("Nguyen Van Combo")
                .contactPhone("0909000111")
                .contactEmail("combo@example.com")
                .adults(2)
                .comboId(comboPackage.getId())
                .voucherCode(voucher.getCode())
                .build());

        Booking bookingAfterCreate = bookingRepository.findById(bookingResponse.getId()).orElseThrow();
        BookingComboItem comboSnapshot = bookingComboItemRepository.findAll().stream()
                .filter(item -> item.getBookingId().equals(bookingAfterCreate.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(bookingAfterCreate.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(bookingAfterCreate.getPaymentStatus()).isEqualTo(BookingPaymentStatus.UNPAID);
        assertThat(bookingAfterCreate.getSubtotalAmount()).isEqualByComparingTo("2000000.00");
        assertThat(bookingAfterCreate.getDiscountAmount()).isEqualByComparingTo("200000.00");
        assertThat(bookingAfterCreate.getVoucherDiscountAmount()).isEqualByComparingTo("150000.00");
        assertThat(bookingAfterCreate.getAddonAmount()).isEqualByComparingTo("100000.00");
        assertThat(bookingAfterCreate.getFinalAmount()).isEqualByComparingTo("1950000.00");
        assertThat(bookingAfterCreate.getVoucherId()).isEqualTo(voucher.getId());
        assertThat(bookingAfterCreate.getComboId()).isEqualTo(comboPackage.getId());

        assertThat(comboSnapshot.getComboId()).isEqualTo(comboPackage.getId());
        assertThat(comboSnapshot.getUnitPrice()).isEqualByComparingTo("150000.00");
        assertThat(comboSnapshot.getDiscountAmount()).isEqualByComparingTo("50000.00");
        assertThat(comboSnapshot.getFinalPrice()).isEqualByComparingTo("100000.00");

        paymentCommandService.createPayment(CreatePaymentRequest.builder()
                .bookingId(bookingAfterCreate.getId())
                .paymentMethod("qr")
                .provider("vnpay")
                .transactionRef("TXN-PHASE3-001")
                .amount(bookingAfterCreate.getFinalAmount())
                .build());

        Booking bookingAfterPayment = bookingRepository.findById(bookingAfterCreate.getId()).orElseThrow();
        Voucher voucherAfterPayment = voucherRepository.findById(voucher.getId()).orElseThrow();
        VoucherUserClaim claimAfterPayment = voucherUserClaimRepository.findById(claim.getId()).orElseThrow();

        assertThat(bookingAfterPayment.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(bookingAfterPayment.getPaymentStatus()).isEqualTo(BookingPaymentStatus.PAID);
        assertThat(voucherAfterPayment.getUsedCount()).isEqualTo(1);
        assertThat(claimAfterPayment.getUsedCount()).isEqualTo(1);
    }

    private Tour seedActiveTour() {
        Destination destination = destinationRepository.save(Destination.builder()
                .code("DST-" + System.nanoTime())
                .name("Nha Trang")
                .slug("nha-trang-" + System.nanoTime())
                .province("Khanh Hoa")
                .district("Nha Trang")
                .region("Mien Trung")
                .address("Nha Trang, Khanh Hoa")
                .shortDescription("Beach destination")
                .description("Beach destination for phase 3 integration test")
                .build());

        return tourRepository.save(Tour.builder()
                .code("TOUR-" + System.nanoTime())
                .name("Coastal Escape")
                .slug("coastal-escape-" + System.nanoTime())
                .destination(destination)
                .basePrice(new BigDecimal("1000000"))
                .currency("VND")
                .durationDays(2)
                .durationNights(1)
                .tripMode("group")
                .transportType("car")
                .status(TourStatus.ACTIVE)
                .build());
    }

    private TourSchedule seedOpenSchedule(Long tourId) {
        LocalDateTime now = LocalDateTime.now();
        return tourScheduleRepository.save(TourSchedule.builder()
                .tourId(tourId)
                .scheduleCode("SCH-" + System.nanoTime())
                .departureAt(now.plusDays(10))
                .returnAt(now.plusDays(11))
                .bookingOpenAt(now.minusDays(2))
                .bookingCloseAt(now.plusDays(5))
                .capacityTotal(10)
                .bookedSeats(0)
                .minGuestsToOperate(1)
                .adultPrice(new BigDecimal("1000000"))
                .childPrice(new BigDecimal("700000"))
                .infantPrice(BigDecimal.ZERO)
                .seniorPrice(new BigDecimal("800000"))
                .status(TourScheduleStatus.OPEN)
                .build());
    }

    private ComboPackage seedActiveComboPackage() {
        ComboPackage comboPackage = ComboPackage.builder()
                .code("COMBO-" + System.nanoTime())
                .name("Adventure Combo")
                .description("Snapshot combo")
                .basePrice(new BigDecimal("150000"))
                .discountAmount(new BigDecimal("50000"))
                .isActive(true)
                .build();

        comboPackage.getItems().add(ComboPackageItem.builder()
                .comboPackage(comboPackage)
                .itemType("other")
                .itemName("Service A")
                .quantity(1)
                .unitPrice(new BigDecimal("150000"))
                .build());

        return comboPackageRepository.save(comboPackage);
    }

    private Voucher seedVoucher(Long tourId) {
        return voucherRepository.save(Voucher.builder()
                .code("SPRING15")
                .name("Spring 15")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("150000"))
                .minOrderValue(new BigDecimal("500000"))
                .usageLimitTotal(10)
                .usageLimitPerUser(1)
                .usedCount(0)
                .applicableScope(VoucherApplicableScope.TOUR)
                .applicableTourId(tourId)
                .applicableMemberLevel(MemberLevel.GOLD)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(5))
                .isActive(true)
                .build());
    }
}
