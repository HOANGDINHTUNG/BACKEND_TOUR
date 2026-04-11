package com.wedservice.backend.module.payments.facade;

import com.wedservice.backend.module.payments.dto.request.CreateRefundRequest;
import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.service.command.RefundCommandService;
import com.wedservice.backend.module.payments.service.query.RefundQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class RefundFacade {

    private final RefundCommandService commandService;
    private final RefundQueryService queryService;

    public RefundResponse createRefund(CreateRefundRequest request) {
        return commandService.createRefundRequest(request);
    }

    public RefundResponse getRefund(Long id) {
        return queryService.getRefund(id);
    }

    public RefundResponse approveRefund(Long id, String processedBy, BigDecimal approvedAmount) {
        return commandService.approveRefund(id, processedBy, approvedAmount);
    }
}
