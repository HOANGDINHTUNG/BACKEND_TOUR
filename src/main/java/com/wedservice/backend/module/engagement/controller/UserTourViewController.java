package com.wedservice.backend.module.engagement.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.engagement.dto.response.ViewedTourResponse;
import com.wedservice.backend.module.engagement.facade.UserTourViewFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/tour-views")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserTourViewController {

    private final UserTourViewFacade userTourViewFacade;

    @GetMapping
    public ApiResponse<List<ViewedTourResponse>> getMyTourViews() {
        return ApiResponse.<List<ViewedTourResponse>>builder()
                .success(true)
                .message("Viewed tours fetched successfully")
                .data(userTourViewFacade.getMyTourViews())
                .build();
    }
}
