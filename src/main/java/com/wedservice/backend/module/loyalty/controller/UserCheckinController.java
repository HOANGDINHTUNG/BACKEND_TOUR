package com.wedservice.backend.module.loyalty.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.loyalty.dto.response.UserCheckinResponse;
import com.wedservice.backend.module.loyalty.facade.UserCheckinFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/checkins")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserCheckinController {

    private final UserCheckinFacade userCheckinFacade;

    @GetMapping
    public ApiResponse<List<UserCheckinResponse>> getMyCheckins() {
        return ApiResponse.<List<UserCheckinResponse>>builder()
                .success(true)
                .message("User checkins fetched successfully")
                .data(userCheckinFacade.getMyCheckins())
                .build();
    }
}
