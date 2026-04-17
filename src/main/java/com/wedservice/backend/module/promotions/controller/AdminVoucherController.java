package com.wedservice.backend.module.promotions.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.promotions.dto.request.UpdateVoucherStatusRequest;
import com.wedservice.backend.module.promotions.dto.request.VoucherRequest;
import com.wedservice.backend.module.promotions.dto.request.VoucherSearchRequest;
import com.wedservice.backend.module.promotions.dto.response.VoucherResponse;
import com.wedservice.backend.module.promotions.facade.AdminVoucherFacade;
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
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminVoucherFacade adminVoucherFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<PageResponse<VoucherResponse>> getVouchers(@Valid @ModelAttribute VoucherSearchRequest request) {
        return ApiResponse.<PageResponse<VoucherResponse>>builder()
                .success(true)
                .message("Voucher list fetched successfully")
                .data(adminVoucherFacade.getVouchers(request))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<VoucherResponse> getVoucher(@PathVariable Long id) {
        return ApiResponse.<VoucherResponse>builder()
                .success(true)
                .message("Voucher fetched successfully")
                .data(adminVoucherFacade.getVoucher(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('voucher.create')")
    public ApiResponse<VoucherResponse> createVoucher(@Valid @RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .success(true)
                .message("Voucher created successfully")
                .data(adminVoucherFacade.createVoucher(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.update')")
    public ApiResponse<VoucherResponse> updateVoucher(@PathVariable Long id, @Valid @RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .success(true)
                .message("Voucher updated successfully")
                .data(adminVoucherFacade.updateVoucher(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('voucher.delete')")
    public ApiResponse<VoucherResponse> updateVoucherStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVoucherStatusRequest request
    ) {
        return ApiResponse.<VoucherResponse>builder()
                .success(true)
                .message("Voucher status updated successfully")
                .data(adminVoucherFacade.updateVoucherStatus(id, request.getIsActive()))
                .build();
    }
}
