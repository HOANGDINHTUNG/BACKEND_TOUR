package com.wedservice.backend.module.promotions.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.promotions.dto.request.UpdateVoucherStatusRequest;
import com.wedservice.backend.module.promotions.dto.request.VoucherRequest;
import com.wedservice.backend.module.promotions.dto.response.VoucherResponse;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.promotions.facade.AdminVoucherFacade;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminVoucherControllerTest {

    @Mock
    private AdminVoucherFacade adminVoucherFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminVoucherController(adminVoucherFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getVouchers_returnsWrappedApiResponse() throws Exception {
        VoucherResponse response = VoucherResponse.builder()
                .id(1L)
                .code("SPRING10")
                .name("Spring 10")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .applicableScope(VoucherApplicableScope.ALL)
                .isActive(true)
                .build();

        when(adminVoucherFacade.getVouchers(any())).thenReturn(
                PageResponse.<VoucherResponse>builder()
                        .content(List.of(response))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build()
        );

        mockMvc.perform(get("/vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voucher list fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].code").value("SPRING10"));
    }

    @Test
    void createVoucher_returnsWrappedApiResponse() throws Exception {
        VoucherRequest request = VoucherRequest.builder()
                .code("SPRING10")
                .name("Spring 10")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .minOrderValue(BigDecimal.ZERO)
                .applicableScope(VoucherApplicableScope.ALL)
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .build();
        VoucherResponse response = VoucherResponse.builder()
                .id(2L)
                .code("SPRING10")
                .name("Spring 10")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .applicableScope(VoucherApplicableScope.ALL)
                .isActive(true)
                .build();

        when(adminVoucherFacade.createVoucher(any(VoucherRequest.class))).thenReturn(response);

        mockMvc.perform(post("/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voucher created successfully"))
                .andExpect(jsonPath("$.data.code").value("SPRING10"));
    }

    @Test
    void updateVoucher_returnsWrappedApiResponse() throws Exception {
        VoucherRequest request = VoucherRequest.builder()
                .code("SPRING10")
                .name("Spring 10 Updated")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("150000"))
                .minOrderValue(BigDecimal.ZERO)
                .applicableScope(VoucherApplicableScope.ALL)
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .build();
        VoucherResponse response = VoucherResponse.builder()
                .id(2L)
                .code("SPRING10")
                .name("Spring 10 Updated")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("150000"))
                .applicableScope(VoucherApplicableScope.ALL)
                .isActive(true)
                .build();

        when(adminVoucherFacade.updateVoucher(org.mockito.ArgumentMatchers.eq(2L), any(VoucherRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/vouchers/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voucher updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Spring 10 Updated"));
    }

    @Test
    void updateVoucherStatus_returnsWrappedApiResponse() throws Exception {
        VoucherResponse response = VoucherResponse.builder()
                .id(2L)
                .code("SPRING10")
                .name("Spring 10")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .applicableScope(VoucherApplicableScope.ALL)
                .isActive(false)
                .build();

        when(adminVoucherFacade.updateVoucherStatus(2L, false)).thenReturn(response);

        mockMvc.perform(patch("/vouchers/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateVoucherStatusRequest.builder()
                                .isActive(false)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voucher status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }
}
