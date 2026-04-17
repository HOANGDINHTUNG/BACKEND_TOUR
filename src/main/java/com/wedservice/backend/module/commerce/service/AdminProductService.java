package com.wedservice.backend.module.commerce.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.commerce.dto.request.ProductRequest;
import com.wedservice.backend.module.commerce.dto.request.ProductSearchRequest;
import com.wedservice.backend.module.commerce.dto.response.ProductResponse;
import com.wedservice.backend.module.commerce.entity.Product;
import com.wedservice.backend.module.commerce.repository.ProductRepository;
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

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final AuditTrailRecorder auditTrailRecorder;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(ProductSearchRequest request) {
        Page<Product> page = productRepository.findAll(
                buildSpecification(request),
                PageRequest.of(request.getPage(), request.getSize(), buildSort(request))
        );

        return PageResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toResponse(findProduct(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateRequest(request);

        String normalizedSku = normalizeRequiredText(request.getSku(), "sku").toUpperCase(Locale.ROOT);
        if (productRepository.findBySkuIgnoreCase(normalizedSku).isPresent()) {
            throw new BadRequestException("Product sku already exists");
        }

        Product product = Product.builder().build();
        applyRequest(product, request, normalizedSku);

        Product savedProduct = productRepository.save(product);
        ProductResponse response = toResponse(savedProduct);
        auditTrailRecorder.record(AuditActionType.PRODUCT_CREATE, savedProduct.getId(), null, response);
        return response;
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        validateRequest(request);

        String normalizedSku = normalizeRequiredText(request.getSku(), "sku").toUpperCase(Locale.ROOT);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(normalizedSku, id)) {
            throw new BadRequestException("Product sku already exists");
        }

        ProductResponse oldState = toResponse(product);
        applyRequest(product, request, normalizedSku);

        Product savedProduct = productRepository.save(product);
        ProductResponse response = toResponse(savedProduct);
        auditTrailRecorder.record(AuditActionType.PRODUCT_UPDATE, savedProduct.getId(), oldState, response);
        return response;
    }

    @Transactional
    public ProductResponse updateProductStatus(Long id, boolean isActive) {
        Product product = findProduct(id);
        ProductResponse oldState = toResponse(product);

        product.setIsActive(isActive);
        Product savedProduct = productRepository.save(product);
        ProductResponse response = toResponse(savedProduct);
        auditTrailRecorder.record(AuditActionType.PRODUCT_STATUS_UPDATE, savedProduct.getId(), oldState, response);
        return response;
    }

    private void applyRequest(Product product, ProductRequest request, String normalizedSku) {
        product.setSku(normalizedSku);
        product.setName(normalizeRequiredText(request.getName(), "name"));
        product.setDescription(normalizeNullable(request.getDescription()));
        product.setProductType(request.getProductType());
        product.setUnitPrice(request.getUnitPrice());
        product.setStockQty(request.getStockQty());
        product.setIsGiftable(request.getIsGiftable() == null ? false : request.getIsGiftable());
        product.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void validateRequest(ProductRequest request) {
        if (request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("unitPrice must be >= 0");
        }
        if (request.getStockQty() < 0) {
            throw new BadRequestException("stockQty must be >= 0");
        }
    }

    private Specification<Product> buildSpecification(ProductSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String keyword = normalizeNullable(request.getKeyword());
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("sku")), likeKeyword),
                        cb.like(cb.lower(root.get("name")), likeKeyword)
                ));
            }

            if (request.getProductType() != null) {
                predicates.add(cb.equal(root.get("productType"), request.getProductType()));
            }

            if (request.getIsGiftable() != null) {
                predicates.add(cb.equal(root.get("isGiftable"), request.getIsGiftable()));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(ProductSearchRequest request) {
        return Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .productType(product.getProductType())
                .unitPrice(product.getUnitPrice())
                .stockQty(product.getStockQty())
                .isGiftable(product.getIsGiftable())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
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
