package com.wedservice.backend.module.commerce.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ProductRequest;
import com.wedservice.backend.module.commerce.dto.request.ProductSearchRequest;
import com.wedservice.backend.module.commerce.dto.response.ProductResponse;
import com.wedservice.backend.module.commerce.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminProductFacade {

    private final AdminProductService adminProductService;

    public PageResponse<ProductResponse> getProducts(ProductSearchRequest request) {
        return adminProductService.getProducts(request);
    }

    public ProductResponse getProduct(Long id) {
        return adminProductService.getProduct(id);
    }

    public ProductResponse createProduct(ProductRequest request) {
        return adminProductService.createProduct(request);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        return adminProductService.updateProduct(id, request);
    }

    public ProductResponse updateProductStatus(Long id, boolean isActive) {
        return adminProductService.updateProductStatus(id, isActive);
    }
}
