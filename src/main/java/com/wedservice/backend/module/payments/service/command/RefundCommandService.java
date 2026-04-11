package com.wedservice.backend.module.payments.service.command;

import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;

import java.math.BigDecimal;

public interface RefundCommandService {
    RefundResponse createRefundRequest(CreateRefundRequest request);
    RefundResponse approveRefund(Long id, String processedBy, BigDecimal approvedAmount);
}
