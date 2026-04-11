package com.wedservice.backend.module.payments.service.query;

import com.wedservice.backend.module.payments.dto.response.RefundResponse;

public interface RefundQueryService {
    RefundResponse getRefund(Long id);
}
