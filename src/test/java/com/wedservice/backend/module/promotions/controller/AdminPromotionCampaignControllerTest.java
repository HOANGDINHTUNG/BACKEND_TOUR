package com.wedservice.backend.module.promotions.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignRequest;
import com.wedservice.backend.module.promotions.dto.request.UpdatePromotionCampaignStatusRequest;
import com.wedservice.backend.module.promotions.dto.response.PromotionCampaignResponse;
import com.wedservice.backend.module.promotions.facade.AdminPromotionCampaignFacade;
import com.wedservice.backend.module.users.entity.MemberLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminPromotionCampaignControllerTest {

    @Mock
    private AdminPromotionCampaignFacade adminPromotionCampaignFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminPromotionCampaignController(adminPromotionCampaignFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getPromotionCampaigns_returnsWrappedApiResponse() throws Exception {
        PromotionCampaignResponse response = PromotionCampaignResponse.builder()
                .id(1L)
                .code("SPRING")
                .name("Spring Campaign")
                .isActive(true)
                .build();

        when(adminPromotionCampaignFacade.getPromotionCampaigns(any())).thenReturn(
                PageResponse.<PromotionCampaignResponse>builder()
                        .content(List.of(response))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build()
        );

        mockMvc.perform(get("/promotion-campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion campaign list fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].code").value("SPRING"));
    }

    @Test
    void createPromotionCampaign_returnsWrappedApiResponse() throws Exception {
        PromotionCampaignRequest request = PromotionCampaignRequest.builder()
                .code("SPRING")
                .name("Spring Campaign")
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .targetMemberLevel(MemberLevel.GOLD)
                .isActive(true)
                .build();
        PromotionCampaignResponse response = PromotionCampaignResponse.builder()
                .id(2L)
                .code("SPRING")
                .name("Spring Campaign")
                .targetMemberLevel(MemberLevel.GOLD)
                .isActive(true)
                .build();

        when(adminPromotionCampaignFacade.createPromotionCampaign(any(PromotionCampaignRequest.class))).thenReturn(response);

        mockMvc.perform(post("/promotion-campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion campaign created successfully"))
                .andExpect(jsonPath("$.data.code").value("SPRING"));
    }

    @Test
    void updatePromotionCampaign_returnsWrappedApiResponse() throws Exception {
        PromotionCampaignRequest request = PromotionCampaignRequest.builder()
                .code("SPRING")
                .name("Spring Campaign Updated")
                .startAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 30, 23, 59))
                .isActive(false)
                .build();
        PromotionCampaignResponse response = PromotionCampaignResponse.builder()
                .id(2L)
                .code("SPRING")
                .name("Spring Campaign Updated")
                .isActive(false)
                .build();

        when(adminPromotionCampaignFacade.updatePromotionCampaign(org.mockito.ArgumentMatchers.eq(2L), any(PromotionCampaignRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/promotion-campaigns/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion campaign updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Spring Campaign Updated"));
    }

    @Test
    void updatePromotionCampaignStatus_returnsWrappedApiResponse() throws Exception {
        PromotionCampaignResponse response = PromotionCampaignResponse.builder()
                .id(2L)
                .code("SPRING")
                .name("Spring Campaign")
                .isActive(false)
                .build();

        when(adminPromotionCampaignFacade.updatePromotionCampaignStatus(2L, false)).thenReturn(response);

        mockMvc.perform(patch("/promotion-campaigns/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdatePromotionCampaignStatusRequest.builder()
                                .isActive(false)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion campaign status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }
}
