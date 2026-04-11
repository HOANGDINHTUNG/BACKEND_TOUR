package com.wedservice.backend.module.payments.service.query.impl;

import com.wedservice.backend.module.payments.dto.response.RefundResponse;
import com.wedservice.backend.module.payments.service.RefundService;
import com.wedservice.backend.module.payments.service.query.RefundQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundQueryServiceImpl implements RefundQueryService {

    private final RefundService refundService;

    @Override
    public RefundResponse getRefund(Long id) {
        return refundService.getRefund(id);
    }
}
