package com.wedservice.backend.module.commerce.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ProductRequest;
import com.wedservice.backend.module.commerce.dto.request.ProductSearchRequest;
import com.wedservice.backend.module.commerce.dto.response.ProductResponse;
import com.wedservice.backend.module.commerce.entity.Product;
import com.wedservice.backend.module.commerce.entity.ProductType;
import com.wedservice.backend.module.commerce.repository.ProductRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    private AdminProductService adminProductService;

    @BeforeEach
    void setUp() {
        adminProductService = new AdminProductService(productRepository, auditTrailRecorder);
    }

    @Test
    void createProduct_normalizesSkuAndRecordsAudit() {
        ProductRequest request = ProductRequest.builder()
                .sku(" sku-001 ")
                .name(" Travel Kit ")
                .description(" Useful gear ")
                .productType(ProductType.GEAR)
                .unitPrice(new BigDecimal("150000"))
                .stockQty(20)
                .isGiftable(true)
                .isActive(true)
                .build();

        when(productRepository.findBySkuIgnoreCase("SKU-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(9L);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            return product;
        });

        ProductResponse response = adminProductService.createProduct(request);

        assertThat(response.getSku()).isEqualTo("SKU-001");
        assertThat(response.getProductType()).isEqualTo(ProductType.GEAR);
        verify(auditTrailRecorder).record(
                org.mockito.ArgumentMatchers.eq(AuditActionType.PRODUCT_CREATE),
                org.mockito.ArgumentMatchers.eq(9L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(ProductResponse.class)
        );
    }

    @Test
    void createProduct_rejectsNegativeStock() {
        ProductRequest request = ProductRequest.builder()
                .sku("SKU-002")
                .name("Insurance")
                .productType(ProductType.INSURANCE)
                .unitPrice(new BigDecimal("50000"))
                .stockQty(-1)
                .build();

        assertThatThrownBy(() -> adminProductService.createProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("stockQty must be >= 0");
    }

    @Test
    void getProducts_returnsPagedResponse() {
        Product product = Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Travel Kit")
                .productType(ProductType.GEAR)
                .unitPrice(new BigDecimal("150000"))
                .stockQty(20)
                .isGiftable(true)
                .isActive(true)
                .build();

        when(productRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1));

        PageResponse<ProductResponse> response = adminProductService.getProducts(new ProductSearchRequest());

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getSku()).isEqualTo("SKU-001");
    }
}
