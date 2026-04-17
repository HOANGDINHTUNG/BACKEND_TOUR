package com.wedservice.backend.module.loyalty.facade;

import com.wedservice.backend.module.loyalty.dto.request.AdminBadgeRequest;
import com.wedservice.backend.module.loyalty.dto.request.UpdateBadgeStatusRequest;
import com.wedservice.backend.module.loyalty.dto.response.BadgeResponse;
import com.wedservice.backend.module.loyalty.dto.response.PassportBadgeResponse;
import com.wedservice.backend.module.loyalty.service.AdminBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminBadgeFacade {

    private final AdminBadgeService adminBadgeService;

    public List<BadgeResponse> getBadges() {
        return adminBadgeService.getBadges();
    }

    public BadgeResponse getBadge(Long id) {
        return adminBadgeService.getBadge(id);
    }

    public BadgeResponse createBadge(AdminBadgeRequest request) {
        return adminBadgeService.createBadge(request);
    }

    public BadgeResponse updateBadge(Long id, AdminBadgeRequest request) {
        return adminBadgeService.updateBadge(id, request);
    }

    public BadgeResponse updateBadgeStatus(Long id, UpdateBadgeStatusRequest request) {
        return adminBadgeService.updateBadgeStatus(id, request);
    }

    public PassportBadgeResponse grantBadge(UUID userId, Long badgeId) {
        return adminBadgeService.grantBadge(userId, badgeId);
    }
}
