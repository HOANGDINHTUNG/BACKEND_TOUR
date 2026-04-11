package com.wedservice.backend.module.payments.service.command;

import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;

public interface PaymentCommandService {
    PaymentResponse createPayment(CreatePaymentRequest request);
}
