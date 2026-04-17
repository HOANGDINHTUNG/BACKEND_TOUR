package com.wedservice.backend.module.promotions.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.promotions.dto.request.ClaimVoucherRequest;
import com.wedservice.backend.module.promotions.dto.response.ClaimedVoucherResponse;
import com.wedservice.backend.module.promotions.entity.ClaimedVoucherStatus;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.promotions.service.UserVoucherService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserVoucherControllerTest {

    @Mock
    private UserVoucherService userVoucherService;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserVoucherController(userVoucherService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyVouchers_returnsWrappedApiResponse() throws Exception {
        ClaimedVoucherResponse response = ClaimedVoucherResponse.builder()
                .claimId(3L)
                .voucherId(11L)
                .voucherCode("SPRING10")
                .voucherName("Spring 10")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .applicableScope(VoucherApplicableScope.ALL)
                .status(ClaimedVoucherStatus.AVAILABLE)
                .build();

        when(userVoucherService.getMyVouchers()).thenReturn(List.of(response));

        mockMvc.perform(get("/users/me/vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User vouchers fetched successfully"))
                .andExpect(jsonPath("$.data[0].voucherCode").value("SPRING10"));
    }

    @Test
    void claimVoucher_returnsCreatedResponse() throws Exception {
        ClaimedVoucherResponse response = ClaimedVoucherResponse.builder()
                .claimId(3L)
                .voucherId(11L)
                .voucherCode("SPRING10")
                .voucherName("Spring 10")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .applicableScope(VoucherApplicableScope.ALL)
                .status(ClaimedVoucherStatus.AVAILABLE)
                .build();

        when(userVoucherService.claimVoucher(any(ClaimVoucherRequest.class))).thenReturn(response);

        mockMvc.perform(post("/vouchers/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ClaimVoucherRequest.builder()
                                .voucherCode("SPRING10")
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voucher claimed successfully"))
                .andExpect(jsonPath("$.data.voucherCode").value("SPRING10"));
    }
}
