package com.wedservice.backend.module.payments.service;

import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;

public interface RefundService {

    RefundResponse createRefundRequest(CreateRefundRequest request);

    RefundResponse getRefund(Long id);

    RefundResponse approveRefund(Long id, String processedBy, java.math.BigDecimal approvedAmount);
}
