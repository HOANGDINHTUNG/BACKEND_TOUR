package com.wedservice.backend.module.payments.facade;

import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.service.command.PaymentCommandService;
import com.wedservice.backend.module.payments.service.query.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        return paymentCommandService.createPayment(request);
    }

    public PaymentResponse getPayment(Long id) {
        return paymentQueryService.getPayment(id);
    }
}

