package com.wedservice.backend.module.promotions.facade;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignRequest;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignSearchRequest;
import com.wedservice.backend.module.promotions.dto.response.PromotionCampaignResponse;
import com.wedservice.backend.module.promotions.service.AdminPromotionCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminPromotionCampaignFacade {

    private final AdminPromotionCampaignService adminPromotionCampaignService;

    public PageResponse<PromotionCampaignResponse> getPromotionCampaigns(PromotionCampaignSearchRequest request) {
        return adminPromotionCampaignService.getPromotionCampaigns(request);
    }

    public PromotionCampaignResponse getPromotionCampaign(Long id) {
        return adminPromotionCampaignService.getPromotionCampaign(id);
    }

    public PromotionCampaignResponse createPromotionCampaign(PromotionCampaignRequest request) {
        return adminPromotionCampaignService.createPromotionCampaign(request);
    }

    public PromotionCampaignResponse updatePromotionCampaign(Long id, PromotionCampaignRequest request) {
        return adminPromotionCampaignService.updatePromotionCampaign(id, request);
    }

    public PromotionCampaignResponse updatePromotionCampaignStatus(Long id, boolean isActive) {
        return adminPromotionCampaignService.updatePromotionCampaignStatus(id, isActive);
    }
}
