package com.wedservice.backend.module.engagement.service.query;

import com.wedservice.backend.module.engagement.dto.response.WishlistTourResponse;

import java.util.List;

public interface UserWishlistQueryService {
    List<WishlistTourResponse> getMyWishlistTours();
}
