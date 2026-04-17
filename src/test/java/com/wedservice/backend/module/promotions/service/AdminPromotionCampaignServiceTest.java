package com.wedservice.backend.module.promotions.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignRequest;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignSearchRequest;
import com.wedservice.backend.module.promotions.dto.response.PromotionCampaignResponse;
import com.wedservice.backend.module.promotions.entity.PromotionCampaign;
import com.wedservice.backend.module.promotions.repository.PromotionCampaignRepository;
import com.wedservice.backend.module.users.entity.MemberLevel;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPromotionCampaignServiceTest {

    @Mock
    private PromotionCampaignRepository promotionCampaignRepository;

    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    private AdminPromotionCampaignService adminPromotionCampaignService;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        adminPromotionCampaignService = new AdminPromotionCampaignService(
                promotionCampaignRepository,
                auditTrailRecorder
        );
        objectMapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Test
    void createPromotionCampaign_normalizesCodeAndRecordsAudit() throws Exception {
        PromotionCampaignRequest request = PromotionCampaignRequest.builder()
                .code(" spring_sale ")
                .name(" Spring Sale ")
                .description(" Limited campaign ")
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .targetMemberLevel(MemberLevel.SILVER)
                .conditionsJson(objectMapper.readTree("{\"minOrder\":2000000}"))
                .rewardJson(objectMapper.readTree("{\"discountPercent\":10}"))
                .isActive(true)
                .build();

        when(promotionCampaignRepository.findByCodeIgnoreCase("SPRING_SALE")).thenReturn(Optional.empty());
        when(promotionCampaignRepository.save(any(PromotionCampaign.class))).thenAnswer(invocation -> {
            PromotionCampaign campaign = invocation.getArgument(0);
            campaign.setId(11L);
            campaign.setCreatedAt(LocalDateTime.now());
            campaign.setUpdatedAt(LocalDateTime.now());
            return campaign;
        });

        PromotionCampaignResponse response = adminPromotionCampaignService.createPromotionCampaign(request);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getCode()).isEqualTo("SPRING_SALE");
        assertThat(response.getName()).isEqualTo("Spring Sale");
        assertThat(response.getDescription()).isEqualTo("Limited campaign");
        assertThat(response.getTargetMemberLevel()).isEqualTo(MemberLevel.SILVER);
        assertThat(response.getConditionsJson().get("minOrder").asInt()).isEqualTo(2000000);
        verify(auditTrailRecorder).record(
                org.mockito.ArgumentMatchers.eq(AuditActionType.PROMOTION_CAMPAIGN_CREATE),
                org.mockito.ArgumentMatchers.eq(11L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(PromotionCampaignResponse.class)
        );
    }

    @Test
    void createPromotionCampaign_rejectsInvalidDateRange() {
        PromotionCampaignRequest request = PromotionCampaignRequest.builder()
                .code("SPRING_SALE")
                .name("Spring Sale")
                .startAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .endAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .build();

        assertThatThrownBy(() -> adminPromotionCampaignService.createPromotionCampaign(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("endAt must be after startAt");
    }

    @Test
    void getPromotionCampaigns_returnsPagedResponse() {
        PromotionCampaignSearchRequest request = new PromotionCampaignSearchRequest();
        PromotionCampaign campaign = PromotionCampaign.builder()
                .id(5L)
                .code("MAY_SALE")
                .name("May Sale")
                .startAt(LocalDateTime.of(2026, 5, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 5, 31, 23, 59))
                .targetMemberLevel(MemberLevel.GOLD)
                .isActive(true)
                .build();

        when(promotionCampaignRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(campaign), PageRequest.of(0, 10), 1));

        PageResponse<PromotionCampaignResponse> response = adminPromotionCampaignService.getPromotionCampaigns(request);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCode()).isEqualTo("MAY_SALE");
    }

    @Test
    void updatePromotionCampaignStatus_updatesFlagAndRecordsAudit() {
        PromotionCampaign campaign = PromotionCampaign.builder()
                .id(7L)
                .code("FLASH")
                .name("Flash")
                .startAt(LocalDateTime.of(2026, 5, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 5, 10, 0, 0))
                .isActive(true)
                .build();

        when(promotionCampaignRepository.findById(7L)).thenReturn(Optional.of(campaign));
        when(promotionCampaignRepository.save(any(PromotionCampaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PromotionCampaignResponse response = adminPromotionCampaignService.updatePromotionCampaignStatus(7L, false);

        assertThat(response.getIsActive()).isFalse();
        verify(auditTrailRecorder).record(
                org.mockito.ArgumentMatchers.eq(AuditActionType.PROMOTION_CAMPAIGN_STATUS_UPDATE),
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.any(PromotionCampaignResponse.class),
                org.mockito.ArgumentMatchers.any(PromotionCampaignResponse.class)
        );
    }
}
