package com.wedservice.backend.module.payments.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.request.ApproveRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.facade.RefundFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundFacade refundService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('refund.create')")
    public ApiResponse<RefundResponse> createRefund(@Valid @RequestBody CreateRefundRequest request) {
    RefundResponse response = refundService.createRefund(request);
        return ApiResponse.success(response, "Refund request created");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('refund.view')")
    public ApiResponse<RefundResponse> getRefund(@PathVariable Long id) {
    return ApiResponse.success(refundService.getRefund(id));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('refund.approve','refund.process')")
    public ApiResponse<RefundResponse> approveRefund(
            @PathVariable Long id,
            @RequestBody ApproveRefundRequest request
    ) {
    RefundResponse response = refundService.approveRefund(id, null, request.getApprovedAmount());
        return ApiResponse.success(response, "Refund approved");
    }
}
