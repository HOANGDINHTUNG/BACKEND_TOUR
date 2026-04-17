package com.wedservice.backend.module.commerce.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageRequest;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageSearchRequest;
import com.wedservice.backend.module.commerce.dto.request.UpdateComboPackageStatusRequest;
import com.wedservice.backend.module.commerce.dto.response.ComboPackageResponse;
import com.wedservice.backend.module.commerce.facade.AdminComboPackageFacade;
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
@RequestMapping("/combo-packages")
@RequiredArgsConstructor
public class AdminComboPackageController {

    private final AdminComboPackageFacade adminComboPackageFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<PageResponse<ComboPackageResponse>> getComboPackages(@Valid @ModelAttribute ComboPackageSearchRequest request) {
        return ApiResponse.<PageResponse<ComboPackageResponse>>builder()
                .success(true)
                .message("Combo package list fetched successfully")
                .data(adminComboPackageFacade.getComboPackages(request))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<ComboPackageResponse> getComboPackage(@PathVariable Long id) {
        return ApiResponse.<ComboPackageResponse>builder()
                .success(true)
                .message("Combo package fetched successfully")
                .data(adminComboPackageFacade.getComboPackage(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('voucher.create')")
    public ApiResponse<ComboPackageResponse> createComboPackage(@Valid @RequestBody ComboPackageRequest request) {
        return ApiResponse.<ComboPackageResponse>builder()
                .success(true)
                .message("Combo package created successfully")
                .data(adminComboPackageFacade.createComboPackage(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.update')")
    public ApiResponse<ComboPackageResponse> updateComboPackage(@PathVariable Long id, @Valid @RequestBody ComboPackageRequest request) {
        return ApiResponse.<ComboPackageResponse>builder()
                .success(true)
                .message("Combo package updated successfully")
                .data(adminComboPackageFacade.updateComboPackage(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('voucher.delete')")
    public ApiResponse<ComboPackageResponse> updateComboPackageStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComboPackageStatusRequest request
    ) {
        return ApiResponse.<ComboPackageResponse>builder()
                .success(true)
                .message("Combo package status updated successfully")
                .data(adminComboPackageFacade.updateComboPackageStatus(id, request.getIsActive()))
                .build();
    }
}
