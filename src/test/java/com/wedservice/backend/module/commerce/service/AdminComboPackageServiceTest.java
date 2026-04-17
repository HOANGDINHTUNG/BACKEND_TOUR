package com.wedservice.backend.module.commerce.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageItemRequest;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageRequest;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageSearchRequest;
import com.wedservice.backend.module.commerce.dto.response.ComboPackageResponse;
import com.wedservice.backend.module.commerce.entity.ComboPackage;
import com.wedservice.backend.module.commerce.entity.ComboPackageItem;
import com.wedservice.backend.module.commerce.entity.Product;
import com.wedservice.backend.module.commerce.entity.ProductType;
import com.wedservice.backend.module.commerce.repository.ComboPackageRepository;
import com.wedservice.backend.module.commerce.repository.ProductRepository;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.repository.TourRepository;
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
class AdminComboPackageServiceTest {

    @Mock
    private ComboPackageRepository comboPackageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    private AdminComboPackageService adminComboPackageService;

    @BeforeEach
    void setUp() {
        adminComboPackageService = new AdminComboPackageService(
                comboPackageRepository,
                productRepository,
                tourRepository,
                auditTrailRecorder
        );
    }

    @Test
    void createComboPackage_persistsItemsAndRecordsAudit() {
        ComboPackageRequest request = ComboPackageRequest.builder()
                .code(" combo-001 ")
                .name(" Adventure Combo ")
                .description(" Combo for adventure travelers ")
                .basePrice(new BigDecimal("250000"))
                .discountAmount(new BigDecimal("50000"))
                .isActive(true)
                .items(List.of(
                        ComboPackageItemRequest.builder()
                                .itemType("product")
                                .itemRefId(7L)
                                .itemName("Travel Kit")
                                .quantity(1)
                                .unitPrice(new BigDecimal("150000"))
                                .build(),
                        ComboPackageItemRequest.builder()
                                .itemType("tour")
                                .itemRefId(9L)
                                .itemName("Island Tour Add-on")
                                .quantity(1)
                                .unitPrice(new BigDecimal("100000"))
                                .build()
                ))
                .build();

        when(comboPackageRepository.findByCodeIgnoreCase("COMBO-001")).thenReturn(Optional.empty());
        when(productRepository.findById(7L)).thenReturn(Optional.of(Product.builder().id(7L).productType(ProductType.GEAR).build()));
        when(tourRepository.findById(9L)).thenReturn(Optional.of(Tour.builder().id(9L).build()));
        when(comboPackageRepository.save(any(ComboPackage.class))).thenAnswer(invocation -> {
            ComboPackage comboPackage = invocation.getArgument(0);
            comboPackage.setId(3L);
            comboPackage.setCreatedAt(LocalDateTime.now());
            comboPackage.setUpdatedAt(LocalDateTime.now());
            long itemId = 1L;
            for (ComboPackageItem item : comboPackage.getItems()) {
                item.setId(itemId++);
            }
            return comboPackage;
        });

        ComboPackage persistedCombo = ComboPackage.builder()
                .id(3L)
                .code("COMBO-001")
                .name("Adventure Combo")
                .description("Combo for adventure travelers")
                .basePrice(new BigDecimal("250000"))
                .discountAmount(new BigDecimal("50000"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of(
                        ComboPackageItem.builder().id(1L).itemType("product").itemRefId(7L).itemName("Travel Kit").quantity(1).unitPrice(new BigDecimal("150000")).build(),
                        ComboPackageItem.builder().id(2L).itemType("tour").itemRefId(9L).itemName("Island Tour Add-on").quantity(1).unitPrice(new BigDecimal("100000")).build()
                ))
                .build();
        when(comboPackageRepository.findDetailById(3L)).thenReturn(Optional.of(persistedCombo));

        ComboPackageResponse response = adminComboPackageService.createComboPackage(request);

        assertThat(response.getCode()).isEqualTo("COMBO-001");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getFinalPrice()).isEqualByComparingTo("200000");
        verify(auditTrailRecorder).record(
                org.mockito.ArgumentMatchers.eq(AuditActionType.COMBO_PACKAGE_CREATE),
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(ComboPackageResponse.class)
        );
    }

    @Test
    void createComboPackage_rejectsBasePriceMismatch() {
        ComboPackageRequest request = ComboPackageRequest.builder()
                .code("COMBO-002")
                .name("Mismatch Combo")
                .basePrice(new BigDecimal("99999"))
                .discountAmount(BigDecimal.ZERO)
                .items(List.of(
                        ComboPackageItemRequest.builder()
                                .itemType("other")
                                .itemName("Service A")
                                .quantity(1)
                                .unitPrice(new BigDecimal("100000"))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> adminComboPackageService.createComboPackage(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("basePrice must equal the sum of combo item list prices");
    }

    @Test
    void getComboPackages_returnsPagedResponse() {
        ComboPackage comboPackage = ComboPackage.builder()
                .id(5L)
                .code("COMBO-001")
                .name("Adventure Combo")
                .basePrice(new BigDecimal("250000"))
                .discountAmount(new BigDecimal("50000"))
                .isActive(true)
                .build();

        when(comboPackageRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(comboPackage), PageRequest.of(0, 10), 1));

        PageResponse<ComboPackageResponse> response = adminComboPackageService.getComboPackages(new ComboPackageSearchRequest());

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCode()).isEqualTo("COMBO-001");
    }
}
