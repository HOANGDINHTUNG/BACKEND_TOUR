package com.wedservice.backend.module.payments.service.query;

import com.wedservice.backend.module.payments.dto.response.PaymentResponse;

public interface PaymentQueryService {
    PaymentResponse getPayment(Long id);
}
