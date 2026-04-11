package com.wedservice.backend.module.payments.service.command.impl;

import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.service.RefundService;
import com.wedservice.backend.module.payments.service.command.RefundCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RefundCommandServiceImpl implements RefundCommandService {

    private final RefundService refundService;

    @Override
    public RefundResponse createRefundRequest(CreateRefundRequest request) {
        return refundService.createRefundRequest(request);
    }

    @Override
    public RefundResponse approveRefund(Long id, String processedBy, BigDecimal approvedAmount) {
        return refundService.approveRefund(id, processedBy, approvedAmount);
    }
}
