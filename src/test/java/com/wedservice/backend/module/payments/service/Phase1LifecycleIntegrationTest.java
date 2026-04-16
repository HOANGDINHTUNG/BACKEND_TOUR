package com.wedservice.backend.module.payments.service;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.dto.request.CreateBookingRequest;
import com.wedservice.backend.module.bookings.entity.Booking;
import com.wedservice.backend.module.bookings.entity.BookingPaymentStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.entity.BookingStatusHistory;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.bookings.repository.BookingStatusHistoryRepository;
import com.wedservice.backend.module.bookings.service.command.BookingCommandService;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.entity.PaymentStatus;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.payments.service.command.PaymentCommandService;
import com.wedservice.backend.module.payments.service.command.RefundCommandService;
import com.wedservice.backend.module.reviews.dto.request.CreateReviewRequest;
import com.wedservice.backend.module.reviews.service.command.ReviewCommandService;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Phase1LifecycleIntegrationTest {

    @Autowired
    private BookingCommandService bookingCommandService;

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private RefundCommandService refundCommandService;

    @Autowired
    private ReviewCommandService reviewCommandService;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingStatusHistoryRepository bookingStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
        when(authenticatedUserProvider.isCurrentUserBackoffice()).thenReturn(false);
    }

    @Test
    void bookingPaymentRefundLifecycle_syncsScheduleTourStatsAndStatusHistory() {
        Tour tour = seedActiveTour();
        TourSchedule schedule = seedOpenSchedule(tour.getId(), 6, new BigDecimal("1200000"));

        var bookingResponse = bookingCommandService.createBooking(CreateBookingRequest.builder()
                .userId(currentUserId.toString())
                .tourId(tour.getId())
                .scheduleId(schedule.getId())
                .contactName("Nguyen Van A")
                .contactPhone("0909000000")
                .contactEmail("a@example.com")
                .adults(2)
                .build());

        Booking bookingAfterCreate = bookingRepository.findById(bookingResponse.getId()).orElseThrow();
        TourSchedule scheduleAfterCreate = tourScheduleRepository.findById(schedule.getId()).orElseThrow();
        Tour tourAfterCreate = tourRepository.findById(tour.getId()).orElseThrow();

        assertThat(bookingAfterCreate.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(bookingAfterCreate.getPaymentStatus()).isEqualTo(BookingPaymentStatus.UNPAID);
        assertThat(scheduleAfterCreate.getBookedSeats()).isEqualTo(2);
        assertThat(tourAfterCreate.getTotalBookings()).isEqualTo(0);

        paymentCommandService.createPayment(CreatePaymentRequest.builder()
                .bookingId(bookingAfterCreate.getId())
                .paymentMethod("qr")
                .provider("vnpay")
                .transactionRef("TXN-PHASE1-001")
                .amount(bookingAfterCreate.getFinalAmount())
                .build());

        Booking bookingAfterPayment = bookingRepository.findById(bookingAfterCreate.getId()).orElseThrow();
        TourSchedule scheduleAfterPayment = tourScheduleRepository.findById(schedule.getId()).orElseThrow();
        Tour tourAfterPayment = tourRepository.findById(tour.getId()).orElseThrow();

        assertThat(bookingAfterPayment.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(bookingAfterPayment.getPaymentStatus()).isEqualTo(BookingPaymentStatus.PAID);
        assertThat(scheduleAfterPayment.getBookedSeats()).isEqualTo(2);
        assertThat(tourAfterPayment.getTotalBookings()).isEqualTo(1);

        RefundResponse refund = createRefundWithQuotedAmount(bookingAfterPayment.getId(), new BigDecimal("1000000"));
        refundCommandService.approveRefund(refund.getId(), null, new BigDecimal("1000000"));

        Booking bookingAfterRefund = bookingRepository.findById(bookingAfterPayment.getId()).orElseThrow();
        TourSchedule scheduleAfterRefund = tourScheduleRepository.findById(schedule.getId()).orElseThrow();
        Tour tourAfterRefund = tourRepository.findById(tour.getId()).orElseThrow();
        List<BookingStatusHistory> history = bookingStatusHistoryRepository.findByBookingIdOrderByCreatedAtAsc(bookingAfterPayment.getId());
        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(payment -> payment.getBookingId().equals(bookingAfterPayment.getId()))
                .toList();

        assertThat(bookingAfterRefund.getStatus()).isEqualTo(BookingStatus.REFUNDED);
        assertThat(bookingAfterRefund.getPaymentStatus()).isEqualTo(BookingPaymentStatus.REFUNDED);
        assertThat(scheduleAfterRefund.getBookedSeats()).isEqualTo(0);
        assertThat(tourAfterRefund.getTotalBookings()).isEqualTo(0);
        assertThat(history).extracting(BookingStatusHistory::getNewStatus)
                .containsExactly(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED, BookingStatus.REFUNDED);
        assertThat(payments).extracting(Payment::getStatus)
                .containsExactlyInAnyOrder(PaymentStatus.PAID, PaymentStatus.REFUNDED);
    }

    @Test
    void completedBookingReviews_syncTourAverageRatingAndTotalReviews() {
        Tour tour = seedActiveTour();
        TourSchedule schedule = seedOpenSchedule(tour.getId(), 10, new BigDecimal("1500000"));
        Booking firstBooking = seedCompletedBooking(tour.getId(), schedule.getId(), currentUserId, "BK-RV-001");
        Booking secondBooking = seedCompletedBooking(tour.getId(), schedule.getId(), currentUserId, "BK-RV-002");

        reviewCommandService.createReview(CreateReviewRequest.builder()
                .bookingId(firstBooking.getId())
                .overallRating(5)
                .title("Excellent")
                .content("Very smooth trip")
                .build());

        Tour tourAfterFirstReview = tourRepository.findById(tour.getId()).orElseThrow();
        assertThat(tourAfterFirstReview.getAverageRating()).isEqualByComparingTo("5.00");
        assertThat(tourAfterFirstReview.getTotalReviews()).isEqualTo(1);

        reviewCommandService.createReview(CreateReviewRequest.builder()
                .bookingId(secondBooking.getId())
                .overallRating(4)
                .title("Good")
                .content("Nice but slightly crowded")
                .build());

        Tour tourAfterSecondReview = tourRepository.findById(tour.getId()).orElseThrow();
        assertThat(tourAfterSecondReview.getAverageRating()).isEqualByComparingTo("4.50");
        assertThat(tourAfterSecondReview.getTotalReviews()).isEqualTo(2);
    }

    private RefundResponse createRefundWithQuotedAmount(Long bookingId, BigDecimal requestedAmount) {
        try (MockedConstruction<SimpleJdbcCall> ignored = mockConstruction(SimpleJdbcCall.class, (mock, context) -> {
            when(mock.withProcedureName("sp_get_refund_quote")).thenReturn(mock);
            when(mock.execute(any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class))).thenReturn(Map.of(
                    "refundable_amount", requestedAmount,
                    "voucher_offer_amount", BigDecimal.ZERO
            ));
        })) {
            return refundCommandService.createRefundRequest(CreateRefundRequest.builder()
                    .bookingId(bookingId)
                    .reasonType("cancel_by_user")
                    .reasonDetail("Need to change plan")
                    .requestedAmount(requestedAmount)
                    .build());
        }
    }

    private Tour seedActiveTour() {
        Destination destination = destinationRepository.save(Destination.builder()
                .code("DST-" + System.nanoTime())
                .name("Da Nang")
                .slug("da-nang-" + System.nanoTime())
                .province("Da Nang")
                .district("Hai Chau")
                .region("Mien Trung")
                .address("Hai Chau, Da Nang")
                .shortDescription("Da Nang destination")
                .description("Public destination for phase 1 integration test")
                .build());

        return tourRepository.save(Tour.builder()
                .code("TOUR-" + System.nanoTime())
                .name("Central Vietnam Escape")
                .slug("central-vietnam-escape-" + System.nanoTime())
                .destination(destination)
                .basePrice(new BigDecimal("1200000"))
                .currency("VND")
                .durationDays(3)
                .durationNights(2)
                .tripMode("group")
                .transportType("car")
                .status(TourStatus.ACTIVE)
                .build());
    }

    private TourSchedule seedOpenSchedule(Long tourId, int capacity, BigDecimal adultPrice) {
        LocalDateTime now = LocalDateTime.now();
        return tourScheduleRepository.save(TourSchedule.builder()
                .tourId(tourId)
                .scheduleCode("SCH-" + System.nanoTime())
                .departureAt(now.plusDays(10))
                .returnAt(now.plusDays(12))
                .bookingOpenAt(now.minusDays(2))
                .bookingCloseAt(now.plusDays(5))
                .capacityTotal(capacity)
                .bookedSeats(0)
                .minGuestsToOperate(1)
                .adultPrice(adultPrice)
                .childPrice(adultPrice.multiply(new BigDecimal("0.7")))
                .infantPrice(BigDecimal.ZERO)
                .seniorPrice(adultPrice.multiply(new BigDecimal("0.8")))
                .status(TourScheduleStatus.OPEN)
                .build());
    }

    private Booking seedCompletedBooking(Long tourId, Long scheduleId, UUID userId, String bookingCode) {
        return bookingRepository.save(Booking.builder()
                .bookingCode(bookingCode)
                .userId(userId)
                .tourId(tourId)
                .scheduleId(scheduleId)
                .status(BookingStatus.COMPLETED)
                .paymentStatus(BookingPaymentStatus.PAID)
                .contactName("Review User")
                .contactPhone("0909000000")
                .adults(1)
                .children(0)
                .infants(0)
                .seniors(0)
                .subtotalAmount(new BigDecimal("1500000"))
                .finalAmount(new BigDecimal("1500000"))
                .currency("VND")
                .build());
    }
}
