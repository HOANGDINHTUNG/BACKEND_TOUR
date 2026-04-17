package com.wedservice.backend.module.tours.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.engagement.facade.UserTourViewFacade;
import com.wedservice.backend.module.tours.dto.response.TourResponse;
import com.wedservice.backend.module.tours.facade.TourFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TourControllerTest {

    @Mock
    private TourFacade tourFacade;

    @Mock
    private UserTourViewFacade userTourViewFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new TourController(tourFacade, userTourViewFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getTour_recordsCurrentUserViewAfterReturningTour() throws Exception {
        TourResponse response = TourResponse.builder()
                .id(10L)
                .code("TOUR-10")
                .name("Viewed Tour")
                .slug("viewed-tour")
                .basePrice(new BigDecimal("1300000"))
                .currency("VND")
                .durationDays(2)
                .durationNights(1)
                .status("active")
                .build();

        when(tourFacade.getTour(10L)).thenReturn(response);

        mockMvc.perform(get("/tours/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));

        verify(userTourViewFacade).recordCurrentUserTourViewIfAuthenticated(10L);
    }
}
