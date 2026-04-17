package com.wedservice.backend.module.commerce.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageRequest;
import com.wedservice.backend.module.commerce.dto.request.ComboPackageSearchRequest;
import com.wedservice.backend.module.commerce.dto.response.ComboPackageResponse;
import com.wedservice.backend.module.commerce.service.AdminComboPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminComboPackageFacade {

    private final AdminComboPackageService adminComboPackageService;

    public PageResponse<ComboPackageResponse> getComboPackages(ComboPackageSearchRequest request) {
        return adminComboPackageService.getComboPackages(request);
    }

    public ComboPackageResponse getComboPackage(Long id) {
        return adminComboPackageService.getComboPackage(id);
    }

    public ComboPackageResponse createComboPackage(ComboPackageRequest request) {
        return adminComboPackageService.createComboPackage(request);
    }

    public ComboPackageResponse updateComboPackage(Long id, ComboPackageRequest request) {
        return adminComboPackageService.updateComboPackage(id, request);
    }

    public ComboPackageResponse updateComboPackageStatus(Long id, boolean isActive) {
        return adminComboPackageService.updateComboPackageStatus(id, isActive);
    }
}
