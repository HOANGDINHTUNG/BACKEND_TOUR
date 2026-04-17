package com.wedservice.backend.module.engagement.service.command;

import com.wedservice.backend.module.engagement.dto.response.WishlistTourResponse;

public interface UserWishlistCommandService {
    WishlistTourResponse addMyWishlistTour(Long tourId);
    void removeMyWishlistTour(Long tourId);
}
