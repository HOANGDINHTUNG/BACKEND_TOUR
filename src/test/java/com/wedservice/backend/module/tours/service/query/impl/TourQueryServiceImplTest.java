package com.wedservice.backend.module.tours.service.query.impl;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.tours.dto.request.TourSearchRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.entity.CancellationPolicy;
import com.wedservice.backend.module.tours.entity.CancellationPolicyRule;
import com.wedservice.backend.module.tours.entity.Guide;
import com.wedservice.backend.module.tours.entity.ItineraryItem;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;
import com.wedservice.backend.module.tours.entity.Tag;
import com.wedservice.backend.module.tours.entity.TourChecklistItem;
import com.wedservice.backend.module.tours.entity.TourItineraryDay;
import com.wedservice.backend.module.tours.entity.TourMedia;
import com.wedservice.backend.module.tours.entity.TourSeasonality;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleGuide;
import com.wedservice.backend.module.tours.entity.TourSchedulePickupPoint;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.repository.ItineraryItemRepository;
import com.wedservice.backend.module.tours.repository.CancellationPolicyRepository;
import com.wedservice.backend.module.tours.repository.CancellationPolicyRuleRepository;
import com.wedservice.backend.module.tours.repository.GuideRepository;
import com.wedservice.backend.module.tours.repository.TagRepository;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourChecklistItemRepository;
import com.wedservice.backend.module.tours.repository.TourItineraryDayRepository;
import com.wedservice.backend.module.tours.repository.TourMediaRepository;
import com.wedservice.backend.module.tours.repository.TourSeasonalityRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleGuideRepository;
import com.wedservice.backend.module.tours.repository.TourSchedulePickupPointRepository;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import com.wedservice.backend.module.tours.repository.TourTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
class TourQueryServiceImplTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private CancellationPolicyRepository cancellationPolicyRepository;

    @Mock
    private CancellationPolicyRuleRepository cancellationPolicyRuleRepository;

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private TourMediaRepository tourMediaRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TourTagRepository tourTagRepository;

    @Mock
    private TourSeasonalityRepository tourSeasonalityRepository;

    @Mock
    private TourItineraryDayRepository tourItineraryDayRepository;

    @Mock
    private ItineraryItemRepository itineraryItemRepository;

    @Mock
    private TourChecklistItemRepository tourChecklistItemRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private TourSchedulePickupPointRepository tourSchedulePickupPointRepository;

    @Mock
    private TourScheduleGuideRepository tourScheduleGuideRepository;

    private TourQueryServiceImpl tourQueryService;

    @BeforeEach
    void setUp() {
        tourQueryService = new TourQueryServiceImpl(
                tourRepository,
                cancellationPolicyRepository,
                cancellationPolicyRuleRepository,
                guideRepository,
                tourMediaRepository,
                tagRepository,
                tourTagRepository,
                tourSeasonalityRepository,
                tourItineraryDayRepository,
                itineraryItemRepository,
                tourChecklistItemRepository,
                tourScheduleRepository,
                tourSchedulePickupPointRepository,
                tourScheduleGuideRepository
        );
    }

    @Test
    void getTourSchedules_returnsOnlyPublicStatuses() {
        Tour tour = Tour.builder()
                .id(15L)
                .code("TOUR-015")
                .name("Sai Gon")
                .slug("sai-gon")
                .destination(Destination.builder().id(1L).build())
                .basePrice(new BigDecimal("1000000"))
                .currency("VND")
                .build();
        TourSchedule openSchedule = TourSchedule.builder()
                .id(1L)
                .tourId(15L)
                .scheduleCode("SCH001")
                .departureAt(LocalDateTime.of(2026, 5, 10, 8, 0))
                .returnAt(LocalDateTime.of(2026, 5, 12, 18, 0))
                .capacityTotal(20)
                .bookedSeats(5)
                .adultPrice(new BigDecimal("1000000"))
                .status(TourScheduleStatus.OPEN)
                .build();
        TourSchedule draftSchedule = TourSchedule.builder()
                .id(2L)
                .tourId(15L)
                .scheduleCode("SCH002")
                .departureAt(LocalDateTime.of(2026, 6, 10, 8, 0))
                .returnAt(LocalDateTime.of(2026, 6, 12, 18, 0))
                .capacityTotal(20)
                .bookedSeats(0)
                .adultPrice(new BigDecimal("1000000"))
                .status(TourScheduleStatus.DRAFT)
                .build();

        when(tourRepository.findById(15L)).thenReturn(Optional.of(tour));
        when(tourScheduleRepository.findByTourId(15L)).thenReturn(List.of(openSchedule, draftSchedule));
        when(tourSchedulePickupPointRepository.findByScheduleIdOrderBySortOrder(1L)).thenReturn(List.of(
                TourSchedulePickupPoint.builder()
                        .id(10L)
                        .scheduleId(1L)
                        .pointName("District 1")
                        .address("123 Le Loi")
                        .sortOrder(1)
                        .build()
        ));
        when(tourScheduleGuideRepository.findByScheduleId(1L)).thenReturn(List.of(
                TourScheduleGuide.builder()
                        .id(20L)
                        .scheduleId(1L)
                        .guideId(99L)
                        .guideRole("lead")
                        .assignedAt(LocalDateTime.of(2026, 4, 1, 9, 0))
                        .build()
        ));
        when(guideRepository.findByIdIn(List.of(99L))).thenReturn(List.of(
                Guide.builder()
                        .id(99L)
                        .code("GD099")
                        .fullName("Le Van Guide")
                        .phone("0909000000")
                        .email("guide99@example.com")
                        .status("active")
                        .isLocalGuide(true)
                        .build()
        ));

        List<TourScheduleResponse> responses = tourQueryService.getTourSchedules(15L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo("open");
        assertThat(responses.get(0).getPickupPoints()).hasSize(1);
        assertThat(responses.get(0).getGuideAssignments()).hasSize(1);
        assertThat(responses.get(0).getGuideAssignments().get(0).getGuideCode()).isEqualTo("GD099");
    }

    @Test
    void getTourSchedule_throwsWhenScheduleDoesNotBelongToTour() {
        Tour tour = Tour.builder()
                .id(15L)
                .code("TOUR-015")
                .name("Sai Gon")
                .slug("sai-gon")
                .build();

        when(tourRepository.findById(15L)).thenReturn(Optional.of(tour));
        when(tourScheduleRepository.findByIdAndTourId(99L, 15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourQueryService.getTourSchedule(15L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tour schedule not found");
    }

    @Test
    void getTour_returnsDetailContent() {
        Tour tour = Tour.builder()
                .id(15L)
                .code("TOUR-015")
                .name("Sai Gon")
                .slug("sai-gon")
                .destination(Destination.builder().id(1L).build())
                .basePrice(new BigDecimal("1000000"))
                .currency("VND")
                .durationDays(2)
                .durationNights(1)
                .cancellationPolicyId(1L)
                .build();
        CancellationPolicy policy = CancellationPolicy.builder()
                .id(1L)
                .name("DEFAULT")
                .description("Default cancellation policy")
                .voucherBonusPercent(new BigDecimal("10"))
                .isDefault(true)
                .isActive(true)
                .build();

        when(tourRepository.findById(15L)).thenReturn(Optional.of(tour));
        when(cancellationPolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(cancellationPolicyRuleRepository.findByPolicyIdOrderByMinHoursBeforeDesc(1L)).thenReturn(List.of(
                CancellationPolicyRule.builder()
                        .id(1L)
                        .policyId(1L)
                        .minHoursBefore(168)
                        .refundPercent(new BigDecimal("80"))
                        .voucherPercent(new BigDecimal("90"))
                        .feePercent(new BigDecimal("20"))
                        .allowReschedule(true)
                        .build()
        ));
        when(tourTagRepository.findByIdTourId(15L)).thenReturn(List.of(
                com.wedservice.backend.module.tours.entity.TourTag.builder()
                        .id(com.wedservice.backend.module.tours.entity.TourTagId.builder().tourId(15L).tagId(10L).build())
                        .build()
        ));
        when(tagRepository.findAllById(List.of(10L))).thenReturn(List.of(
                Tag.builder()
                        .id(10L)
                        .code("GIA_DINH")
                        .name("Gia đình")
                        .tagGroup("doi_tuong")
                        .description("Family friendly")
                        .isActive(true)
                        .build()
        ));
        when(tourMediaRepository.findByTourIdOrderBySortOrder(15L)).thenReturn(List.of(
                TourMedia.builder()
                        .id(11L)
                        .tourId(15L)
                        .mediaType("image")
                        .mediaUrl("https://cdn.example.com/tours/sg-cover.jpg")
                        .sortOrder(0)
                        .isActive(true)
                        .build()
        ));
        when(tourItineraryDayRepository.findByTourIdOrderByDayNumber(15L)).thenReturn(List.of(
                TourItineraryDay.builder()
                        .id(21L)
                        .tourId(15L)
                        .dayNumber(1)
                        .title("Day 1")
                        .build()
        ));
        when(itineraryItemRepository.findByItineraryDayIdOrderBySequenceNo(21L)).thenReturn(List.of(
                ItineraryItem.builder()
                        .id(31L)
                        .itineraryDayId(21L)
                        .sequenceNo(1)
                        .itemType("visit")
                        .title("Ben Thanh")
                        .build()
        ));
        when(tourSeasonalityRepository.findByTourId(15L)).thenReturn(List.of(
                TourSeasonality.builder()
                        .id(51L)
                        .tourId(15L)
                        .seasonName("Peak")
                        .monthFrom(5)
                        .monthTo(8)
                        .recommendationScore(new BigDecimal("9.5"))
                        .notes("Best weather")
                        .build()
        ));
        when(tourChecklistItemRepository.findByTourId(15L)).thenReturn(List.of(
                TourChecklistItem.builder()
                        .id(41L)
                        .tourId(15L)
                        .itemName("Nước uống")
                        .itemGroup("packing")
                        .isRequired(false)
                        .build()
        ));

        TourResponse response = tourQueryService.getTour(15L);

        assertThat(response.getTags()).hasSize(1);
        assertThat(response.getMedia()).hasSize(1);
        assertThat(response.getSeasonality()).hasSize(1);
        assertThat(response.getItineraryDays()).hasSize(1);
        assertThat(response.getItineraryDays().get(0).getItems()).hasSize(1);
        assertThat(response.getChecklistItems()).hasSize(1);
        assertThat(response.getCancellationPolicy()).isNotNull();
        assertThat(response.getCancellationPolicy().getRules()).hasSize(1);
    }

    @Test
    void searchTours_appliesActiveDestinationKeywordAndTagFilters() {
        TourSearchRequest request = TourSearchRequest.builder()
                .destinationId(3L)
                .keyword("Da Nang")
                .tagIds(List.of(10L, 11L))
                .minPrice(new BigDecimal("900000"))
                .maxPrice(new BigDecimal("1500000"))
                .travelMonth(6)
                .sortBy("basePrice")
                .sortDir("asc")
                .page(1)
                .size(5)
                .build();
        Tour tour = Tour.builder()
                .id(15L)
                .code("TOUR-015")
                .name("Da Nang Discovery")
                .slug("da-nang-discovery")
                .destination(Destination.builder().id(3L).build())
                .basePrice(new BigDecimal("1000000"))
                .currency("VND")
                .status(com.wedservice.backend.module.tours.entity.TourStatus.ACTIVE)
                .build();

        when(tourTagRepository.findByIdTagIdIn(List.of(10L, 11L))).thenReturn(List.of(
                com.wedservice.backend.module.tours.entity.TourTag.builder()
                        .id(com.wedservice.backend.module.tours.entity.TourTagId.builder().tourId(15L).tagId(10L).build())
                        .build()
        ));
        when(tourSeasonalityRepository.findActiveByTravelMonth(6)).thenReturn(List.of(
                TourSeasonality.builder()
                        .id(51L)
                        .tourId(15L)
                        .seasonName("Peak")
                        .monthFrom(5)
                        .monthTo(8)
                        .recommendationScore(new BigDecimal("9.5"))
                        .build()
        ));
        when(tourRepository.findAll(any(com.querydsl.core.BooleanBuilder.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tour), PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "basePrice")), 1));

        var page = tourQueryService.searchTours(request);

        ArgumentCaptor<com.querydsl.core.BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(com.querydsl.core.BooleanBuilder.class);
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(tourRepository).findAll(predicateCaptor.capture(), pageRequestCaptor.capture());

        assertThat(page.getContent()).hasSize(1);
        assertThat(pageRequestCaptor.getValue()).isEqualTo(PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "basePrice")));
        assertThat(predicateCaptor.getValue().toString())
                .contains("ACTIVE")
                .contains("3")
                .contains("Da Nang")
                .contains("900000")
                .contains("1500000")
                .contains("15");
    }

    @Test
    void searchTours_returnsEmptyPageWhenTagFilterHasNoMatch() {
        TourSearchRequest request = TourSearchRequest.builder()
                .tagIds(List.of(999L))
                .page(0)
                .size(10)
                .build();

        when(tourTagRepository.findByIdTagIdIn(List.of(999L))).thenReturn(List.of());

        var page = tourQueryService.searchTours(request);

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void searchTours_returnsEmptyPageWhenTravelMonthHasNoMatch() {
        TourSearchRequest request = TourSearchRequest.builder()
                .travelMonth(12)
                .page(0)
                .size(10)
                .build();

        when(tourSeasonalityRepository.findActiveByTravelMonth(12)).thenReturn(List.of());

        var page = tourQueryService.searchTours(request);

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void searchTours_appliesFeaturedAndAudienceFilters() {
        TourSearchRequest request = TourSearchRequest.builder()
                .featuredOnly(true)
                .studentFriendlyOnly(true)
                .familyFriendlyOnly(true)
                .seniorFriendlyOnly(true)
                .page(0)
                .size(10)
                .build();
        Tour tour = Tour.builder()
                .id(16L)
                .code("TOUR-016")
                .name("Hue Family Tour")
                .slug("hue-family-tour")
                .destination(Destination.builder().id(4L).build())
                .basePrice(new BigDecimal("1200000"))
                .currency("VND")
                .isFeatured(true)
                .isStudentFriendly(true)
                .isFamilyFriendly(true)
                .isSeniorFriendly(true)
                .status(com.wedservice.backend.module.tours.entity.TourStatus.ACTIVE)
                .build();

        when(tourRepository.findAll(any(com.querydsl.core.BooleanBuilder.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tour), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1));

        var page = tourQueryService.searchTours(request);

        ArgumentCaptor<com.querydsl.core.BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(com.querydsl.core.BooleanBuilder.class);
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(tourRepository).findAll(predicateCaptor.capture(), pageRequestCaptor.capture());

        assertThat(page.getContent()).hasSize(1);
        assertThat(pageRequestCaptor.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        assertThat(predicateCaptor.getValue().toString())
                .contains("isFeatured")
                .contains("isStudentFriendly")
                .contains("isFamilyFriendly")
                .contains("isSeniorFriendly");
    }

    @Test
    void searchTours_appliesDifficultyActivityAndDurationFilters() {
        TourSearchRequest request = TourSearchRequest.builder()
                .difficultyLevel(3)
                .activityLevel(4)
                .minDurationDays(2)
                .maxDurationDays(5)
                .sortBy("durationDays")
                .sortDir("desc")
                .page(0)
                .size(10)
                .build();
        Tour tour = Tour.builder()
                .id(17L)
                .code("TOUR-017")
                .name("Adventure Da Lat")
                .slug("adventure-da-lat")
                .destination(Destination.builder().id(5L).build())
                .basePrice(new BigDecimal("1800000"))
                .currency("VND")
                .difficultyLevel(3)
                .activityLevel(4)
                .durationDays(4)
                .status(com.wedservice.backend.module.tours.entity.TourStatus.ACTIVE)
                .build();

        when(tourRepository.findAll(any(com.querydsl.core.BooleanBuilder.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tour), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "durationDays")), 1));

        var page = tourQueryService.searchTours(request);

        ArgumentCaptor<com.querydsl.core.BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(com.querydsl.core.BooleanBuilder.class);
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(tourRepository).findAll(predicateCaptor.capture(), pageRequestCaptor.capture());

        assertThat(page.getContent()).hasSize(1);
        assertThat(pageRequestCaptor.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "durationDays")));
        assertThat(predicateCaptor.getValue().toString())
                .contains("difficultyLevel = 3")
                .contains("activityLevel = 4")
                .contains("durationDays >= 2")
                .contains("durationDays <= 5");
    }

    @Test
    void searchTours_throwsWhenMaxDurationIsSmallerThanMinDuration() {
        TourSearchRequest request = TourSearchRequest.builder()
                .minDurationDays(5)
                .maxDurationDays(3)
                .build();

        assertThatThrownBy(() -> tourQueryService.searchTours(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("maxDurationDays must be greater than or equal to minDurationDays");
    }

    @Test
    void searchTours_appliesTravellerSuitabilityTripModeAndTransportFilters() {
        TourSearchRequest request = TourSearchRequest.builder()
                .travellerAge(18)
                .groupSize(4)
                .tripMode("private")
                .transportType("car")
                .page(0)
                .size(10)
                .build();
        Tour tour = Tour.builder()
                .id(18L)
                .code("TOUR-018")
                .name("Private Nha Trang Escape")
                .slug("private-nha-trang-escape")
                .destination(Destination.builder().id(6L).build())
                .basePrice(new BigDecimal("2200000"))
                .currency("VND")
                .tripMode("private")
                .transportType("limousine car")
                .minAge(12)
                .maxAge(65)
                .minGroupSize(2)
                .maxGroupSize(6)
                .status(com.wedservice.backend.module.tours.entity.TourStatus.ACTIVE)
                .build();

        when(tourRepository.findAll(any(com.querydsl.core.BooleanBuilder.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tour), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1));

        var page = tourQueryService.searchTours(request);

        ArgumentCaptor<com.querydsl.core.BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(com.querydsl.core.BooleanBuilder.class);
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(tourRepository).findAll(predicateCaptor.capture(), pageRequestCaptor.capture());

        assertThat(page.getContent()).hasSize(1);
        assertThat(pageRequestCaptor.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        assertThat(predicateCaptor.getValue().toString())
                .contains("minAge")
                .contains("maxAge")
                .contains("minGroupSize")
                .contains("maxGroupSize")
                .contains("private")
                .contains("car");
    }

    @Test
    void searchTours_appliesMinRatingFilter() {
        TourSearchRequest request = TourSearchRequest.builder()
                .minRating(new BigDecimal("4.50"))
                .page(0)
                .size(10)
                .build();
        Tour tour = Tour.builder()
                .id(19L)
                .code("TOUR-019")
                .name("Premium Ha Giang Loop")
                .slug("premium-ha-giang-loop")
                .destination(Destination.builder().id(7L).build())
                .basePrice(new BigDecimal("3200000"))
                .currency("VND")
                .averageRating(new BigDecimal("4.80"))
                .status(com.wedservice.backend.module.tours.entity.TourStatus.ACTIVE)
                .build();

        when(tourRepository.findAll(any(com.querydsl.core.BooleanBuilder.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tour), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1));

        var page = tourQueryService.searchTours(request);

        ArgumentCaptor<com.querydsl.core.BooleanBuilder> predicateCaptor = ArgumentCaptor.forClass(com.querydsl.core.BooleanBuilder.class);
        verify(tourRepository).findAll(predicateCaptor.capture(), any(PageRequest.class));

        assertThat(page.getContent()).hasSize(1);
        assertThat(predicateCaptor.getValue().toString()).contains("4.50");
    }
}
