package com.wedservice.backend.module.loyalty.facade;

import com.wedservice.backend.module.loyalty.dto.response.TravelPassportResponse;
import com.wedservice.backend.module.loyalty.service.UserPassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPassportFacade {

    private final UserPassportService userPassportService;

    public TravelPassportResponse getMyPassport() {
        return userPassportService.getMyPassport();
    }
}
