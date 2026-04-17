package com.wedservice.backend.module.engagement.facade;

import com.wedservice.backend.module.engagement.dto.response.ViewedTourResponse;
import com.wedservice.backend.module.engagement.service.command.UserTourViewCommandService;
import com.wedservice.backend.module.engagement.service.query.UserTourViewQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserTourViewFacade {

    private final UserTourViewCommandService commandService;
    private final UserTourViewQueryService queryService;

    public void recordCurrentUserTourViewIfAuthenticated(Long tourId) {
        commandService.recordCurrentUserTourViewIfAuthenticated(tourId);
    }

    public List<ViewedTourResponse> getMyTourViews() {
        return queryService.getMyTourViews();
    }
}
