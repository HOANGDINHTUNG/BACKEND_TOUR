package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.loyalty.dto.response.TravelPassportResponse;
import com.wedservice.backend.module.loyalty.facade.UserPassportFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/passport")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserPassportController {

    private final UserPassportFacade userPassportFacade;

    @GetMapping
    public ApiResponse<TravelPassportResponse> getMyPassport() {
        return ApiResponse.<TravelPassportResponse>builder()
                .success(true)
                .message("Travel passport fetched successfully")
                .data(userPassportFacade.getMyPassport())
                .build();
    }
}
