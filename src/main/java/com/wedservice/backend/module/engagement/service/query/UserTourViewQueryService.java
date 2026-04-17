package com.wedservice.backend.module.engagement.service.query;

import com.wedservice.backend.module.engagement.dto.response.ViewedTourResponse;

import java.util.List;

public interface UserTourViewQueryService {
    List<ViewedTourResponse> getMyTourViews();
}
