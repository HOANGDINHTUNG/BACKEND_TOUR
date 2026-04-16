package com.wedservice.backend.module.tours.service.command.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.tours.dto.request.ItineraryItemRequest;
import com.wedservice.backend.module.tours.dto.request.TourChecklistItemRequest;
import com.wedservice.backend.module.tours.dto.request.TourItineraryDayRequest;
import com.wedservice.backend.module.tours.dto.request.TourMediaRequest;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleGuideRequest;
import com.wedservice.backend.module.tours.dto.request.TourSchedulePickupPointRequest;
import com.wedservice.backend.module.tours.dto.request.TourScheduleRequest;
import com.wedservice.backend.module.tours.dto.request.TourSeasonalityRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.dto.response.TourScheduleResponse;
import com.wedservice.backend.module.tours.entity.CancellationPolicy;
import com.wedservice.backend.module.tours.entity.CancellationPolicyRule;
import com.wedservice.backend.module.tours.entity.Guide;
import com.wedservice.backend.module.tours.entity.ItineraryItem;
import com.wedservice.backend.module.tours.entity.Tag;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourChecklistItem;
import com.wedservice.backend.module.tours.entity.TourItineraryDay;
import com.wedservice.backend.module.tours.entity.TourMedia;
import com.wedservice.backend.module.tours.entity.TourSeasonality;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.entity.TourScheduleGuide;
import com.wedservice.backend.module.tours.entity.TourSchedulePickupPoint;
import com.wedservice.backend.module.tours.entity.TourScheduleStatus;
import com.wedservice.backend.module.tours.entity.TourStatus;
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
import com.wedservice.backend.module.tours.validator.TourValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourCommandServiceImplTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private DestinationRepository destinationRepository;

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

    private TourCommandServiceImpl tourCommandService;

    @BeforeEach
    void setUp() {
        tourCommandService = new TourCommandServiceImpl(
                tourRepository,
                destinationRepository,
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
                tourScheduleGuideRepository,
                new TourValidator()
        );
    }

    @Test
    void createTour_bindsDestinationAndNormalizesStatusFields() {
        TourRequest request = TourRequest.builder()
                .code(" TOUR-001 ")
                .name(" Ha Noi City Tour ")
                .slug(" ha-noi-city-tour ")
                .destinationId(5L)
                .basePrice(new BigDecimal("1250000"))
                .currency("vnd")
                .durationDays(3)
                .durationNights(2)
                .status("ACTIVE")
                .isFeatured(true)
                .build();

        Destination destination = Destination.builder()
                .id(5L)
                .code("HN")
                .name("Ha Noi")
                .slug("ha-noi")
                .countryCode("VN")
                .province("Ha Noi")
                .build();
        CancellationPolicy defaultPolicy = CancellationPolicy.builder()
                .id(1L)
                .name("DEFAULT")
                .isDefault(true)
                .isActive(true)
                .build();

        when(destinationRepository.findById(5L)).thenReturn(Optional.of(destination));
        when(cancellationPolicyRepository.findFirstByIsDefaultTrueAndIsActiveTrue()).thenReturn(Optional.of(defaultPolicy));
        when(cancellationPolicyRuleRepository.existsByPolicyId(1L)).thenReturn(true);
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });
        when(cancellationPolicyRepository.findById(1L)).thenReturn(Optional.of(defaultPolicy));
        when(cancellationPolicyRuleRepository.findByPolicyIdOrderByMinHoursBeforeDesc(1L)).thenReturn(java.util.List.of(
                CancellationPolicyRule.builder()
                        .id(1L)
                        .policyId(1L)
                        .refundPercent(new BigDecimal("80"))
                        .voucherPercent(new BigDecimal("90"))
                        .feePercent(new BigDecimal("20"))
                        .allowReschedule(true)
                        .build()
        ));

        TourResponse response = tourCommandService.createTour(request);

        ArgumentCaptor<Tour> captor = ArgumentCaptor.forClass(Tour.class);
        verify(tourRepository).save(captor.capture());
        Tour savedTour = captor.getValue();

        assertThat(savedTour.getCode()).isEqualTo("TOUR-001");
        assertThat(savedTour.getName()).isEqualTo("Ha Noi City Tour");
        assertThat(savedTour.getSlug()).isEqualTo("ha-noi-city-tour");
        assertThat(savedTour.getDestination()).isSameAs(destination);
        assertThat(savedTour.getCancellationPolicyId()).isEqualTo(1L);
        assertThat(savedTour.getCurrency()).isEqualTo("VND");
        assertThat(savedTour.getStatus()).isEqualTo(TourStatus.ACTIVE);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getDestinationId()).isEqualTo(5L);
        assertThat(response.getCurrency()).isEqualTo("VND");
    }

    @Test
    void createTour_persistsMediaItineraryAndChecklist() {
        TourRequest request = TourRequest.builder()
                .code("TOUR-003")
                .name("Hue Heritage")
                .slug("hue-heritage")
                .destinationId(8L)
                .basePrice(new BigDecimal("1800000"))
                .durationDays(2)
                .durationNights(1)
                .shortDescription("Short intro")
                .description("Long intro")
                .tagIds(java.util.List.of(10L))
                .media(java.util.List.of(TourMediaRequest.builder()
                        .mediaType("image")
                        .mediaUrl("https://cdn.example.com/tours/hue-cover.jpg")
                        .altText("Hue cover")
                        .sortOrder(0)
                        .build()))
                .seasonality(java.util.List.of(TourSeasonalityRequest.builder()
                        .seasonName("Peak")
                        .monthFrom(5)
                        .monthTo(8)
                        .recommendationScore(new BigDecimal("9.5"))
                        .notes("Best weather")
                        .build()))
                .itineraryDays(java.util.List.of(TourItineraryDayRequest.builder()
                        .dayNumber(1)
                        .title("Arrival")
                        .items(java.util.List.of(ItineraryItemRequest.builder()
                                .sequenceNo(1)
                                .itemType("visit")
                                .title("Imperial City")
                                .build()))
                        .build()))
                .checklistItems(java.util.List.of(TourChecklistItemRequest.builder()
                        .itemName("Giày thể thao")
                        .itemGroup("packing")
                        .isRequired(true)
                        .build()))
                .build();

        Destination destination = Destination.builder()
                .id(8L)
                .code("HUE")
                .name("Hue")
                .slug("hue")
                .countryCode("VN")
                .province("Thua Thien Hue")
                .build();
        CancellationPolicy defaultPolicy = CancellationPolicy.builder()
                .id(1L)
                .name("DEFAULT")
                .isDefault(true)
                .isActive(true)
                .build();

        when(destinationRepository.findById(8L)).thenReturn(Optional.of(destination));
        when(cancellationPolicyRepository.findFirstByIsDefaultTrueAndIsActiveTrue()).thenReturn(Optional.of(defaultPolicy));
        when(cancellationPolicyRuleRepository.existsByPolicyId(1L)).thenReturn(true);
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });
        when(cancellationPolicyRepository.findById(1L)).thenReturn(Optional.of(defaultPolicy));
        when(cancellationPolicyRuleRepository.findByPolicyIdOrderByMinHoursBeforeDesc(1L)).thenReturn(java.util.List.of(
                CancellationPolicyRule.builder()
                        .id(1L)
                        .policyId(1L)
                        .refundPercent(new BigDecimal("80"))
                        .voucherPercent(new BigDecimal("90"))
                        .feePercent(new BigDecimal("20"))
                        .allowReschedule(true)
                        .build()
        ));
        when(tagRepository.findByIdInAndIsActiveTrue(java.util.List.of(10L))).thenReturn(java.util.List.of(
                Tag.builder()
                        .id(10L)
                        .code("GIA_DINH")
                        .name("Gia đình")
                        .tagGroup("doi_tuong")
                        .description("Family friendly")
                        .isActive(true)
                        .build()
        ));
        when(tourTagRepository.findByIdTourId(20L)).thenReturn(java.util.List.of(
                com.wedservice.backend.module.tours.entity.TourTag.builder()
                        .id(com.wedservice.backend.module.tours.entity.TourTagId.builder().tourId(20L).tagId(10L).build())
                        .build()
        ));
        when(tagRepository.findAllById(java.util.List.of(10L))).thenReturn(java.util.List.of(
                Tag.builder()
                        .id(10L)
                        .code("GIA_DINH")
                        .name("Gia đình")
                        .tagGroup("doi_tuong")
                        .description("Family friendly")
                        .isActive(true)
                        .build()
        ));
        when(tourMediaRepository.findByTourIdOrderBySortOrder(20L)).thenReturn(java.util.List.of(
                TourMedia.builder()
                        .id(1L)
                        .tourId(20L)
                        .mediaType("image")
                        .mediaUrl("https://cdn.example.com/tours/hue-cover.jpg")
                        .altText("Hue cover")
                        .sortOrder(0)
                        .isActive(true)
                        .build()
        ));
        when(tourItineraryDayRepository.save(any(TourItineraryDay.class))).thenAnswer(invocation -> {
            TourItineraryDay day = invocation.getArgument(0);
            day.setId(30L);
            return day;
        });
        when(tourItineraryDayRepository.findByTourIdOrderByDayNumber(20L)).thenReturn(java.util.List.of(
                TourItineraryDay.builder()
                        .id(30L)
                        .tourId(20L)
                        .dayNumber(1)
                        .title("Arrival")
                        .build()
        ));
        when(itineraryItemRepository.findByItineraryDayIdOrderBySequenceNo(30L)).thenReturn(java.util.List.of(
                ItineraryItem.builder()
                        .id(40L)
                        .itineraryDayId(30L)
                        .sequenceNo(1)
                        .itemType("visit")
                        .title("Imperial City")
                        .build()
        ));
        when(tourSeasonalityRepository.findByTourId(20L)).thenReturn(java.util.List.of(
                TourSeasonality.builder()
                        .id(60L)
                        .tourId(20L)
                        .seasonName("Peak")
                        .monthFrom(5)
                        .monthTo(8)
                        .recommendationScore(new BigDecimal("9.5"))
                        .notes("Best weather")
                        .build()
        ));
        when(tourChecklistItemRepository.findByTourId(20L)).thenReturn(java.util.List.of(
                TourChecklistItem.builder()
                        .id(50L)
                        .tourId(20L)
                        .itemName("Giày thể thao")
                        .itemGroup("packing")
                        .isRequired(true)
                        .build()
        ));

        TourResponse response = tourCommandService.createTour(request);

        verify(tourMediaRepository).saveAll(any());
        verify(tourTagRepository).saveAll(any());
        verify(tourSeasonalityRepository).saveAll(any());
        verify(itineraryItemRepository).saveAll(any());
        verify(tourChecklistItemRepository).saveAll(any());
        assertThat(response.getTags()).hasSize(1);
        assertThat(response.getMedia()).hasSize(1);
        assertThat(response.getSeasonality()).hasSize(1);
        assertThat(response.getItineraryDays()).hasSize(1);
        assertThat(response.getChecklistItems()).hasSize(1);
        assertThat(response.getCancellationPolicy()).isNotNull();
    }

    @Test
    void createTour_throwsBadRequestWhenDestinationWasDeleted() {
        TourRequest request = TourRequest.builder()
                .code("TOUR-002")
                .name("Da Nang Tour")
                .slug("da-nang-tour")
                .destinationId(7L)
                .basePrice(BigDecimal.ONE)
                .durationDays(2)
                .build();

        Destination deletedDestination = Destination.builder()
                .id(7L)
                .code("DN")
                .name("Da Nang")
                .slug("da-nang")
                .countryCode("VN")
                .province("Da Nang")
                .build();
        deletedDestination.setDeletedAt(LocalDateTime.now());

        when(destinationRepository.findById(7L)).thenReturn(Optional.of(deletedDestination));

        assertThatThrownBy(() -> tourCommandService.createTour(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Destination has been deleted");
    }

    @Test
    void createTourSchedule_persistsSchedulePickupPointsAndGuideAssignments() {
        Tour tour = Tour.builder()
                .id(15L)
                .code("TOUR-015")
                .name("Sai Gon City")
                .slug("sai-gon-city")
                .build();

        TourScheduleRequest request = TourScheduleRequest.builder()
                .departureAt(LocalDateTime.of(2026, 5, 10, 8, 0))
                .returnAt(LocalDateTime.of(2026, 5, 12, 18, 0))
                .bookingOpenAt(LocalDateTime.of(2026, 4, 1, 0, 0))
                .bookingCloseAt(LocalDateTime.of(2026, 5, 9, 23, 0))
                .meetingAt(LocalDateTime.of(2026, 5, 10, 7, 30))
                .meetingPointName("Ben Thanh")
                .meetingAddress("District 1")
                .capacityTotal(20)
                .minGuestsToOperate(5)
                .adultPrice(new BigDecimal("1000000"))
                .childPrice(new BigDecimal("600000"))
                .status("open")
                .pickupPoints(java.util.List.of(TourSchedulePickupPointRequest.builder()
                        .pointName("District 1")
                        .address("123 Le Loi")
                        .sortOrder(1)
                        .pickupAt(LocalDateTime.of(2026, 5, 10, 7, 0))
                        .build()))
                .guideAssignments(java.util.List.of(TourScheduleGuideRequest.builder()
                        .guideId(99L)
                        .guideRole("lead")
                        .build()))
                .build();

        when(tourRepository.findById(15L)).thenReturn(Optional.of(tour));
        when(tourScheduleRepository.save(any(TourSchedule.class))).thenAnswer(invocation -> {
            TourSchedule schedule = invocation.getArgument(0);
            schedule.setId(66L);
            return schedule;
        });
        when(tourSchedulePickupPointRepository.findByScheduleIdOrderBySortOrder(66L)).thenReturn(java.util.List.of(
                TourSchedulePickupPoint.builder()
                        .id(1L)
                        .scheduleId(66L)
                        .pointName("District 1")
                        .address("123 Le Loi")
                        .sortOrder(1)
                        .pickupAt(LocalDateTime.of(2026, 5, 10, 7, 0))
                        .build()
        ));
        when(tourScheduleGuideRepository.findByScheduleId(66L)).thenReturn(java.util.List.of(
                TourScheduleGuide.builder()
                        .id(2L)
                        .scheduleId(66L)
                        .guideId(99L)
                        .guideRole("lead")
                        .assignedAt(LocalDateTime.of(2026, 4, 1, 9, 0))
                        .build()
        ));
        when(guideRepository.findByIdIn(java.util.List.of(99L))).thenReturn(java.util.List.of(
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

        TourScheduleResponse response = tourCommandService.createTourSchedule(15L, request);

        ArgumentCaptor<TourSchedule> scheduleCaptor = ArgumentCaptor.forClass(TourSchedule.class);
        verify(tourScheduleRepository).save(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getTourId()).isEqualTo(15L);
        assertThat(scheduleCaptor.getValue().getStatus()).isEqualTo(TourScheduleStatus.OPEN);

        verify(tourSchedulePickupPointRepository).saveAll(any());
        verify(tourScheduleGuideRepository).saveAll(any());

        assertThat(response.getTourId()).isEqualTo(15L);
        assertThat(response.getPickupPoints()).hasSize(1);
        assertThat(response.getGuideAssignments()).hasSize(1);
        assertThat(response.getGuideAssignments().get(0).getGuideFullName()).isEqualTo("Le Van Guide");
        assertThat(response.getStatus()).isEqualTo("open");
    }

    @Test
    void createTourSchedule_rejectsInactiveGuideAssignments() {
        Tour tour = Tour.builder()
                .id(15L)
                .code("TOUR-015")
                .name("Sai Gon City")
                .slug("sai-gon-city")
                .build();

        TourScheduleRequest request = TourScheduleRequest.builder()
                .departureAt(LocalDateTime.of(2026, 5, 10, 8, 0))
                .returnAt(LocalDateTime.of(2026, 5, 12, 18, 0))
                .capacityTotal(20)
                .adultPrice(new BigDecimal("1000000"))
                .guideAssignments(java.util.List.of(TourScheduleGuideRequest.builder()
                        .guideId(88L)
                        .guideRole("lead")
                        .build()))
                .build();

        when(tourRepository.findById(15L)).thenReturn(Optional.of(tour));
        when(tourScheduleRepository.save(any(TourSchedule.class))).thenAnswer(invocation -> {
            TourSchedule schedule = invocation.getArgument(0);
            schedule.setId(77L);
            return schedule;
        });
        when(guideRepository.findByIdIn(java.util.List.of(88L))).thenReturn(java.util.List.of(
                Guide.builder()
                        .id(88L)
                        .code("GD088")
                        .fullName("Inactive Guide")
                        .status("inactive")
                        .isLocalGuide(true)
                        .build()
        ));

        assertThatThrownBy(() -> tourCommandService.createTourSchedule(15L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only active guides can be assigned to schedules");
    }
}
