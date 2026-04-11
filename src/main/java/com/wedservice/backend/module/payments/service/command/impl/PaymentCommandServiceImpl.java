package com.wedservice.backend.module.payments.service.command.impl;

import com.wedservice.backend.module.payments.dto.request.CreatePaymentRequest;
import com.wedservice.backend.module.payments.dto.response.PaymentResponse;
import com.wedservice.backend.module.payments.entity.Payment;
import com.wedservice.backend.module.payments.repository.PaymentRepository;
import com.wedservice.backend.module.payments.service.command.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment p = Payment.builder()
                .paymentCode("PM" + System.currentTimeMillis())
                .bookingId(request.getBookingId())
                .paymentMethod(request.getPaymentMethod())
                .provider(request.getProvider())
                .transactionRef(request.getTransactionRef())
                .amount(request.getAmount())
                .currency("VND")
                .status("paid")
                .paidAt(LocalDateTime.now())
                .build();

        p = paymentRepository.save(p);

        return PaymentResponse.builder()
                .id(p.getId())
                .paymentCode(p.getPaymentCode())
                .bookingId(p.getBookingId())
                .amount(p.getAmount())
                .status(p.getStatus())
                .build();
    }
}
