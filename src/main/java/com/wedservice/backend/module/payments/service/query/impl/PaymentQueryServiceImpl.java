package com.wedservice.backend.module.payments.service.query.impl;

import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.payments.service.query.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse getPayment(Long id) {
        Payment p = paymentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        return PaymentResponse.builder()
                .id(p.getId())
                .paymentCode(p.getPaymentCode())
                .bookingId(p.getBookingId())
                .amount(p.getAmount())
                .status(p.getStatus())
                .build();
    }
}
