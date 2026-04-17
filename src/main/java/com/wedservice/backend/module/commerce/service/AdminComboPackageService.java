package com.wedservice.backend.module.commerce.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageItemRequest;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageRequest;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageSearchRequest;
import com.wedservice.backend.module.commerce.dto.response.ComboPackageItemResponse;
import com.wedservice.backend.module.commerce.dto.response.ComboPackageResponse;
import com.wedservice.backend.module.commerce.entity.ComboPackage;
import com.wedservice.backend.module.commerce.entity.ComboPackageItem;
import com.wedservice.backend.module.commerce.repository.ComboPackageRepository;
import com.wedservice.backend.module.commerce.repository.ProductRepository;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminComboPackageService {

    private static final Set<String> ALLOWED_ITEM_TYPES = Set.of("product", "tour", "service", "gift", "other");

    private final ComboPackageRepository comboPackageRepository;
    private final ProductRepository productRepository;
    private final TourRepository tourRepository;
    private final AuditTrailRecorder auditTrailRecorder;

    @Transactional(readOnly = true)
    public PageResponse<ComboPackageResponse> getComboPackages(ComboPackageSearchRequest request) {
        Page<ComboPackage> page = comboPackageRepository.findAll(
                buildSpecification(request),
                PageRequest.of(request.getPage(), request.getSize(), buildSort(request))
        );

        return PageResponse.<ComboPackageResponse>builder()
                .content(page.getContent().stream().map(combo -> toResponse(combo, false)).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public ComboPackageResponse getComboPackage(Long id) {
        return toResponse(findComboPackageDetail(id), true);
    }

    @Transactional
    public ComboPackageResponse createComboPackage(ComboPackageRequest request) {
        validateRequest(request);

        String normalizedCode = normalizeRequiredText(request.getCode(), "code").toUpperCase(Locale.ROOT);
        if (comboPackageRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new BadRequestException("Combo package code already exists");
        }

        ComboPackage comboPackage = ComboPackage.builder().build();
        applyRequest(comboPackage, request, normalizedCode);

        ComboPackage savedComboPackage = comboPackageRepository.save(comboPackage);
        ComboPackageResponse response = toResponse(findComboPackageDetail(savedComboPackage.getId()), true);
        auditTrailRecorder.record(AuditActionType.COMBO_PACKAGE_CREATE, savedComboPackage.getId(), null, response);
        return response;
    }

    @Transactional
    public ComboPackageResponse updateComboPackage(Long id, ComboPackageRequest request) {
        ComboPackage comboPackage = findComboPackageDetail(id);
        validateRequest(request);

        String normalizedCode = normalizeRequiredText(request.getCode(), "code").toUpperCase(Locale.ROOT);
        if (comboPackageRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new BadRequestException("Combo package code already exists");
        }

        ComboPackageResponse oldState = toResponse(comboPackage, true);
        applyRequest(comboPackage, request, normalizedCode);

        ComboPackage savedComboPackage = comboPackageRepository.save(comboPackage);
        ComboPackageResponse response = toResponse(findComboPackageDetail(savedComboPackage.getId()), true);
        auditTrailRecorder.record(AuditActionType.COMBO_PACKAGE_UPDATE, savedComboPackage.getId(), oldState, response);
        return response;
    }

    @Transactional
    public ComboPackageResponse updateComboPackageStatus(Long id, boolean isActive) {
        ComboPackage comboPackage = findComboPackage(id);
        ComboPackageResponse oldState = toResponse(findComboPackageDetail(id), true);

        comboPackage.setIsActive(isActive);
        ComboPackage savedComboPackage = comboPackageRepository.save(comboPackage);
        ComboPackageResponse response = toResponse(findComboPackageDetail(savedComboPackage.getId()), true);
        auditTrailRecorder.record(AuditActionType.COMBO_PACKAGE_STATUS_UPDATE, savedComboPackage.getId(), oldState, response);
        return response;
    }

    private void applyRequest(ComboPackage comboPackage, ComboPackageRequest request, String normalizedCode) {
        comboPackage.setCode(normalizedCode);
        comboPackage.setName(normalizeRequiredText(request.getName(), "name"));
        comboPackage.setDescription(normalizeNullable(request.getDescription()));
        comboPackage.setBasePrice(request.getBasePrice());
        comboPackage.setDiscountAmount(request.getDiscountAmount());
        comboPackage.setIsActive(request.getIsActive() == null ? true : request.getIsActive());

        comboPackage.getItems().clear();
        for (ComboPackageItemRequest itemRequest : request.getItems()) {
            comboPackage.getItems().add(toEntity(comboPackage, itemRequest));
        }
    }

    private ComboPackageItem toEntity(ComboPackage comboPackage, ComboPackageItemRequest request) {
        return ComboPackageItem.builder()
                .comboPackage(comboPackage)
                .itemType(normalizeItemType(request.getItemType()))
                .itemRefId(request.getItemRefId())
                .itemName(normalizeRequiredText(request.getItemName(), "itemName"))
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();
    }

    private void validateRequest(ComboPackageRequest request) {
        if (request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("basePrice must be >= 0");
        }
        if (request.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("discountAmount must be >= 0");
        }
        if (request.getDiscountAmount().compareTo(request.getBasePrice()) > 0) {
            throw new BadRequestException("discountAmount must be <= basePrice");
        }

        BigDecimal computedBasePrice = BigDecimal.ZERO;
        for (ComboPackageItemRequest itemRequest : request.getItems()) {
            validateItemRequest(itemRequest);
            computedBasePrice = computedBasePrice.add(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        if (computedBasePrice.compareTo(request.getBasePrice()) != 0) {
            throw new BadRequestException("basePrice must equal the sum of combo item list prices");
        }
    }

    private void validateItemRequest(ComboPackageItemRequest request) {
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("quantity must be >= 1");
        }
        if (request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("unitPrice must be >= 0");
        }

        String itemType = normalizeItemType(request.getItemType());
        if (!ALLOWED_ITEM_TYPES.contains(itemType)) {
            throw new BadRequestException("itemType is invalid");
        }

        if ("product".equals(itemType)) {
            if (request.getItemRefId() == null) {
                throw new BadRequestException("itemRefId is required when itemType is product");
            }
            productRepository.findById(request.getItemRefId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getItemRefId()));
        }

        if ("tour".equals(itemType)) {
            if (request.getItemRefId() == null) {
                throw new BadRequestException("itemRefId is required when itemType is tour");
            }
            tourRepository.findById(request.getItemRefId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + request.getItemRefId()));
        }
    }

    private ComboPackage findComboPackage(Long id) {
        return comboPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Combo package not found with id: " + id));
    }

    private ComboPackage findComboPackageDetail(Long id) {
        return comboPackageRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Combo package not found with id: " + id));
    }

    private Specification<ComboPackage> buildSpecification(ComboPackageSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String keyword = normalizeNullable(request.getKeyword());
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), likeKeyword),
                        cb.like(cb.lower(root.get("name")), likeKeyword)
                ));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(ComboPackageSearchRequest request) {
        return Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
    }

    private ComboPackageResponse toResponse(ComboPackage comboPackage, boolean includeItems) {
        BigDecimal finalPrice = comboPackage.getBasePrice().subtract(comboPackage.getDiscountAmount());
        return ComboPackageResponse.builder()
                .id(comboPackage.getId())
                .code(comboPackage.getCode())
                .name(comboPackage.getName())
                .description(comboPackage.getDescription())
                .basePrice(comboPackage.getBasePrice())
                .discountAmount(comboPackage.getDiscountAmount())
                .finalPrice(finalPrice.max(BigDecimal.ZERO))
                .isActive(comboPackage.getIsActive())
                .items(includeItems ? comboPackage.getItems().stream().map(this::toResponse).toList() : null)
                .createdAt(comboPackage.getCreatedAt())
                .updatedAt(comboPackage.getUpdatedAt())
                .build();
    }

    private ComboPackageItemResponse toResponse(ComboPackageItem item) {
        return ComboPackageItemResponse.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .itemRefId(item.getItemRefId())
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private String normalizeItemType(String value) {
        return normalizeRequiredText(value, "itemType").toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}
