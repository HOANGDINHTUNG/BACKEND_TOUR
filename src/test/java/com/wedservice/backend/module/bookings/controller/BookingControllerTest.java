package com.wedservice.backend.module.bookings.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.bookings.dto.request.BookingQuoteRequest;
import com.wedservice.backend.module.bookings.dto.response.AppliedVoucherQuoteResponse;
import com.wedservice.backend.module.bookings.dto.response.BookingQuoteResponse;
import com.wedservice.backend.module.bookings.facade.BookingFacade;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingFacade bookingFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new BookingController(bookingFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void quoteBooking_returnsWrappedApiResponse() throws Exception {
        BookingQuoteResponse response = BookingQuoteResponse.builder()
                .tourId(10L)
                .scheduleId(22L)
                .adults(2)
                .children(1)
                .infants(1)
                .seniors(1)
                .seatCount(4)
                .travellerCount(5)
                .subtotalAmount(new BigDecimal("350.00"))
                .discountAmount(new BigDecimal("20.00"))
                .voucherDiscountAmount(new BigDecimal("20.00"))
                .loyaltyDiscountAmount(BigDecimal.ZERO)
                .addonAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("330.00"))
                .currency("VND")
                .appliedVoucher(AppliedVoucherQuoteResponse.builder()
                        .claimId(9L)
                        .voucherId(5L)
                        .voucherCode("SPRING10")
                        .voucherName("Spring 10")
                        .discountType(VoucherDiscountType.PERCENTAGE)
                        .discountValue(BigDecimal.TEN)
                        .maxDiscountAmount(new BigDecimal("20.00"))
                        .build())
                .build();

        when(bookingFacade.quoteBooking(any(BookingQuoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/bookings/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BookingQuoteRequest.builder()
                                .tourId(10L)
                                .scheduleId(22L)
                                .adults(2)
                                .children(1)
                                .infants(1)
                                .seniors(1)
                                .voucherCode("SPRING10")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking quote calculated"))
                .andExpect(jsonPath("$.data.finalAmount").value(330.00))
                .andExpect(jsonPath("$.data.appliedVoucher.voucherCode").value("SPRING10"));
    }
}
