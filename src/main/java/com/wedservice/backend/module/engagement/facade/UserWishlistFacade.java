package com.wedservice.backend.module.engagement.facade;

import com.wedservice.backend.module.engagement.dto.response.WishlistTourResponse;
import com.wedservice.backend.module.engagement.service.command.UserWishlistCommandService;
import com.wedservice.backend.module.engagement.service.query.UserWishlistQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserWishlistFacade {

    private final UserWishlistCommandService commandService;
    private final UserWishlistQueryService queryService;

    public List<WishlistTourResponse> getMyWishlistTours() {
        return queryService.getMyWishlistTours();
    }

    public WishlistTourResponse addMyWishlistTour(Long tourId) {
        return commandService.addMyWishlistTour(tourId);
    }

    public void removeMyWishlistTour(Long tourId) {
        commandService.removeMyWishlistTour(tourId);
    }
}
