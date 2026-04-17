package com.wedservice.backend.module.commerce.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ProductRequest;
import com.wedservice.backend.module.commerce.dto.request.UpdateProductStatusRequest;
import com.wedservice.backend.module.commerce.dto.response.ProductResponse;
import com.wedservice.backend.module.commerce.entity.ProductType;
import com.wedservice.backend.module.commerce.facade.AdminProductFacade;
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
class AdminProductControllerTest {

    @Mock
    private AdminProductFacade adminProductFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(adminProductFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getProducts_returnsWrappedApiResponse() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Travel Kit")
                .productType(ProductType.GEAR)
                .unitPrice(new BigDecimal("150000"))
                .stockQty(20)
                .isGiftable(true)
                .isActive(true)
                .build();

        when(adminProductFacade.getProducts(any())).thenReturn(
                PageResponse.<ProductResponse>builder()
                        .content(List.of(response))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build()
        );

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product list fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU-001"));
    }

    @Test
    void createProduct_returnsWrappedApiResponse() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .sku("SKU-001")
                .name("Travel Kit")
                .productType(ProductType.GEAR)
                .unitPrice(new BigDecimal("150000"))
                .stockQty(20)
                .isGiftable(true)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Travel Kit")
                .productType(ProductType.GEAR)
                .unitPrice(new BigDecimal("150000"))
                .stockQty(20)
                .isGiftable(true)
                .isActive(true)
                .build();

        when(adminProductFacade.createProduct(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.sku").value("SKU-001"));
    }

    @Test
    void updateProductStatus_returnsWrappedApiResponse() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Travel Kit")
                .productType(ProductType.GEAR)
                .unitPrice(new BigDecimal("150000"))
                .stockQty(20)
                .isGiftable(true)
                .isActive(false)
                .build();

        when(adminProductFacade.updateProductStatus(1L, false)).thenReturn(response);

        mockMvc.perform(patch("/products/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateProductStatusRequest.builder().isActive(false).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }
}
