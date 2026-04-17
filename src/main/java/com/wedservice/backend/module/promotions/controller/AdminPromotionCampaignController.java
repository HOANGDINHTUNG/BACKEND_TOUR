package com.wedservice.backend.module.promotions.controller;

import com.wedservice.backend.common.response.ApiResponse;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignRequest;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignSearchRequest;
import com.wedservice.backend.module.promotions.dto.request.UpdatePromotionCampaignStatusRequest;
import com.wedservice.backend.module.promotions.dto.response.PromotionCampaignResponse;
import com.wedservice.backend.module.promotions.facade.AdminPromotionCampaignFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotion-campaigns")
@RequiredArgsConstructor
public class AdminPromotionCampaignController {

    private final AdminPromotionCampaignFacade adminPromotionCampaignFacade;

    @GetMapping
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<PageResponse<PromotionCampaignResponse>> getPromotionCampaigns(
            @Valid @ModelAttribute PromotionCampaignSearchRequest request
    ) {
        return ApiResponse.<PageResponse<PromotionCampaignResponse>>builder()
                .success(true)
                .message("Promotion campaign list fetched successfully")
                .data(adminPromotionCampaignFacade.getPromotionCampaigns(request))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.view')")
    public ApiResponse<PromotionCampaignResponse> getPromotionCampaign(@PathVariable Long id) {
        return ApiResponse.<PromotionCampaignResponse>builder()
                .success(true)
                .message("Promotion campaign fetched successfully")
                .data(adminPromotionCampaignFacade.getPromotionCampaign(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('voucher.create')")
    public ApiResponse<PromotionCampaignResponse> createPromotionCampaign(@Valid @RequestBody PromotionCampaignRequest request) {
        return ApiResponse.<PromotionCampaignResponse>builder()
                .success(true)
                .message("Promotion campaign created successfully")
                .data(adminPromotionCampaignFacade.createPromotionCampaign(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.update')")
    public ApiResponse<PromotionCampaignResponse> updatePromotionCampaign(
            @PathVariable Long id,
            @Valid @RequestBody PromotionCampaignRequest request
    ) {
        return ApiResponse.<PromotionCampaignResponse>builder()
                .success(true)
                .message("Promotion campaign updated successfully")
                .data(adminPromotionCampaignFacade.updatePromotionCampaign(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('voucher.delete')")
    public ApiResponse<PromotionCampaignResponse> updatePromotionCampaignStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromotionCampaignStatusRequest request
    ) {
        return ApiResponse.<PromotionCampaignResponse>builder()
                .success(true)
                .message("Promotion campaign status updated successfully")
                .data(adminPromotionCampaignFacade.updatePromotionCampaignStatus(id, request.getIsActive()))
                .build();
    }
}
