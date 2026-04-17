package com.wedservice.backend.module.loyalty.facade;

import com.wedservice.backend.module.loyalty.dto.response.UserCheckinResponse;
import com.wedservice.backend.module.loyalty.service.UserPassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCheckinFacade {

    private final UserPassportService userPassportService;

    public List<UserCheckinResponse> getMyCheckins() {
        return userPassportService.getMyCheckins();
    }
}
