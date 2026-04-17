package com.wedservice.backend.module.engagement.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.module.engagement.dto.response.WishlistTourResponse;
import com.wedservice.backend.module.engagement.facade.UserWishlistFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/wishlist/tours")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserWishlistController {

    private final UserWishlistFacade userWishlistFacade;

    @GetMapping
    public ApiResponse<List<WishlistTourResponse>> getMyWishlistTours() {
        return ApiResponse.<List<WishlistTourResponse>>builder()
                .success(true)
                .message("Wishlist tours fetched successfully")
                .data(userWishlistFacade.getMyWishlistTours())
                .build();
    }

    @PostMapping("/{tourId}")
    public ApiResponse<WishlistTourResponse> addMyWishlistTour(@PathVariable Long tourId) {
        return ApiResponse.<WishlistTourResponse>builder()
                .success(true)
                .message("Tour added to wishlist successfully")
                .data(userWishlistFacade.addMyWishlistTour(tourId))
                .build();
    }

    @DeleteMapping("/{tourId}")
    public ApiResponse<Void> removeMyWishlistTour(@PathVariable Long tourId) {
        userWishlistFacade.removeMyWishlistTour(tourId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Tour removed from wishlist successfully")
                .build();
    }
}
