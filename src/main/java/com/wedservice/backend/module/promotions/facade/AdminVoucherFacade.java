package com.wedservice.backend.module.promotions.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.promotions.dto.request.VoucherRequest;
import com.wedservice.backend.module.promotions.dto.request.VoucherSearchRequest;
import com.wedservice.backend.module.promotions.dto.response.VoucherResponse;
import com.wedservice.backend.module.promotions.service.AdminVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminVoucherFacade {

    private final AdminVoucherService adminVoucherService;

    public PageResponse<VoucherResponse> getVouchers(VoucherSearchRequest request) {
        return adminVoucherService.getVouchers(request);
    }

    public VoucherResponse getVoucher(Long id) {
        return adminVoucherService.getVoucher(id);
    }

    public VoucherResponse createVoucher(VoucherRequest request) {
        return adminVoucherService.createVoucher(request);
    }

    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        return adminVoucherService.updateVoucher(id, request);
    }

    public VoucherResponse updateVoucherStatus(Long id, boolean isActive) {
        return adminVoucherService.updateVoucherStatus(id, isActive);
    }
}
