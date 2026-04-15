package com.wedservice.backend.module.tours.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.tours.dto.request.TourRequest;
import com.wedservice.backend.module.tours.entity.TourStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class TourValidator {

    public void validateRequest(TourRequest request) {
        if (request.getDurationDays() != null
                && request.getDurationNights() != null
                && request.getDurationNights() > request.getDurationDays()) {
            throw new BadRequestException("Duration nights cannot be greater than duration days");
        }
    }

    public String normalizeCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return "VND";
        }
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 3) {
            throw new BadRequestException("Currency must be a 3-character code");
        }
        return normalized;
    }

    public TourStatus normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return TourStatus.DRAFT;
        }
        try {
            return TourStatus.fromValue(status.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}
