package com.wedservice.backend.module.commerce.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageItemRequest;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageRequest;
import com.wedservice.backend.module.commerce.dto.request.UpdateComboPackageStatusRequest;
import com.wedservice.backend.module.commerce.dto.response.ComboPackageItemResponse;
import com.wedservice.backend.module.commerce.dto.response.ComboPackageResponse;
import com.wedservice.backend.module.commerce.facade.AdminComboPackageFacade;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminComboPackageControllerTest {

    @Mock
    private AdminComboPackageFacade adminComboPackageFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminComboPackageController(adminComboPackageFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getComboPackages_returnsWrappedApiResponse() throws Exception {
        ComboPackageResponse response = ComboPackageResponse.builder()
                .id(1L)
                .code("COMBO-001")
                .name("Adventure Combo")
                .basePrice(new BigDecimal("250000"))
                .discountAmount(new BigDecimal("50000"))
                .finalPrice(new BigDecimal("200000"))
                .isActive(true)
                .build();

        when(adminComboPackageFacade.getComboPackages(any())).thenReturn(
                PageResponse.<ComboPackageResponse>builder()
                        .content(List.of(response))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build()
        );

        mockMvc.perform(get("/combo-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Combo package list fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].code").value("COMBO-001"));
    }

    @Test
    void createComboPackage_returnsWrappedApiResponse() throws Exception {
        ComboPackageRequest request = ComboPackageRequest.builder()
                .code("COMBO-001")
                .name("Adventure Combo")
                .basePrice(new BigDecimal("250000"))
                .discountAmount(new BigDecimal("50000"))
                .items(List.of(
                        ComboPackageItemRequest.builder()
                                .itemType("other")
                                .itemName("Service A")
                                .quantity(1)
                                .unitPrice(new BigDecimal("250000"))
                                .build()
                ))
                .build();

        ComboPackageResponse response = ComboPackageResponse.builder()
                .id(1L)
                .code("COMBO-001")
                .name("Adventure Combo")
                .basePrice(new BigDecimal("250000"))
                .discountAmount(new BigDecimal("50000"))
                .finalPrice(new BigDecimal("200000"))
                .isActive(true)
                .items(List.of(
                        ComboPackageItemResponse.builder()
                                .id(1L)
                                .itemType("other")
                                .itemName("Service A")
                                .quantity(1)
                                .unitPrice(new BigDecimal("250000"))
                                .lineTotal(new BigDecimal("250000"))
                                .build()
                ))
                .build();

        when(adminComboPackageFacade.createComboPackage(any(ComboPackageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/combo-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Combo package created successfully"))
                .andExpect(jsonPath("$.data.code").value("COMBO-001"));
    }

    @Test
    void updateComboPackageStatus_returnsWrappedApiResponse() throws Exception {
        ComboPackageResponse response = ComboPackageResponse.builder()
                .id(1L)
                .code("COMBO-001")
                .name("Adventure Combo")
                .basePrice(new BigDecimal("250000"))
                .discountAmount(new BigDecimal("50000"))
                .finalPrice(new BigDecimal("200000"))
                .isActive(false)
                .build();

        when(adminComboPackageFacade.updateComboPackageStatus(1L, false)).thenReturn(response);

        mockMvc.perform(patch("/combo-packages/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateComboPackageStatusRequest.builder().isActive(false).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Combo package status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }
}
