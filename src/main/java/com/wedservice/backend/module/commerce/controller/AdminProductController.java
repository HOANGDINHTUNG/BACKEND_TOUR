package com.wedservice.backend.module.commerce.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ProductRequest;
import com.wedservice.backend.module.commerce.dto.request.ProductSearchRequest;
import com.wedservice.backend.module.commerce.dto.request.UpdateProductStatusRequest;
import com.wedservice.backend.module.commerce.dto.response.ProductResponse;
import com.wedservice.backend.module.commerce.facade.AdminProductFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductFacade adminProductFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<PageResponse<ProductResponse>> getProducts(@Valid @ModelAttribute ProductSearchRequest request) {
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .success(true)
                .message("Product list fetched successfully")
                .data(adminProductFacade.getProducts(request))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product fetched successfully")
                .data(adminProductFacade.getProduct(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('voucher.create')")
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product created successfully")
                .data(adminProductFacade.createProduct(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.update')")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product updated successfully")
                .data(adminProductFacade.updateProduct(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('voucher.delete')")
    public ApiResponse<ProductResponse> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product status updated successfully")
                .data(adminProductFacade.updateProductStatus(id, request.getIsActive()))
                .build();
    }
}
