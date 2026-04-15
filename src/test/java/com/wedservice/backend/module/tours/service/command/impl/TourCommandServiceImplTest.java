package com.wedservice.backend.module.tours.service.command.impl;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TourRepository;
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

    private TourCommandServiceImpl tourCommandService;

    @BeforeEach
    void setUp() {
        tourCommandService = new TourCommandServiceImpl(tourRepository, destinationRepository, new TourValidator());
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

        when(destinationRepository.findById(5L)).thenReturn(Optional.of(destination));
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        TourResponse response = tourCommandService.createTour(request);

        ArgumentCaptor<Tour> captor = ArgumentCaptor.forClass(Tour.class);
        verify(tourRepository).save(captor.capture());
        Tour savedTour = captor.getValue();

        assertThat(savedTour.getCode()).isEqualTo("TOUR-001");
        assertThat(savedTour.getName()).isEqualTo("Ha Noi City Tour");
        assertThat(savedTour.getSlug()).isEqualTo("ha-noi-city-tour");
        assertThat(savedTour.getDestination()).isSameAs(destination);
        assertThat(savedTour.getCurrency()).isEqualTo("VND");
        assertThat(savedTour.getStatus()).isEqualTo(TourStatus.ACTIVE);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getDestinationId()).isEqualTo(5L);
        assertThat(response.getCurrency()).isEqualTo("VND");
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
}
