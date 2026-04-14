package com.wedservice.backend.module.payments.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.facade.PaymentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('payment.create')")
    public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
    PaymentResponse response = paymentFacade.createPayment(request);
        return ApiResponse.success(response, "Payment created");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('payment.view')")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable Long id) {
    return ApiResponse.success(paymentFacade.getPayment(id));
    }
}
