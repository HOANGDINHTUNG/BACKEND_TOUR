package com.wedservice.backend.module.promotions.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.promotions.dto.request.ClaimVoucherRequest;
import com.wedservice.backend.module.promotions.dto.response.ClaimedVoucherResponse;
import com.wedservice.backend.module.promotions.service.UserVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserVoucherController {

    private final UserVoucherService userVoucherService;

    @GetMapping("/users/me/vouchers")
    public ApiResponse<List<ClaimedVoucherResponse>> getMyVouchers() {
        return ApiResponse.<List<ClaimedVoucherResponse>>builder()
                .success(true)
                .message("User vouchers fetched successfully")
                .data(userVoucherService.getMyVouchers())
                .build();
    }

    @PostMapping("/vouchers/claim")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClaimedVoucherResponse> claimVoucher(@Valid @RequestBody ClaimVoucherRequest request) {
        return ApiResponse.<ClaimedVoucherResponse>builder()
                .success(true)
                .message("Voucher claimed successfully")
                .data(userVoucherService.claimVoucher(request))
                .build();
    }
}
