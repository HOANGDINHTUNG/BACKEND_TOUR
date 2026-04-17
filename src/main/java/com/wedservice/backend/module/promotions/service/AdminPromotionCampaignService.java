package com.wedservice.backend.module.promotions.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignRequest;
import com.wedservice.backend.module.promotions.dto.request.PromotionCampaignSearchRequest;
import com.wedservice.backend.module.promotions.dto.response.PromotionCampaignResponse;
import com.wedservice.backend.module.promotions.entity.PromotionCampaign;
import com.wedservice.backend.module.promotions.repository.PromotionCampaignRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminPromotionCampaignService {

    private final PromotionCampaignRepository promotionCampaignRepository;
    private final AuditTrailRecorder auditTrailRecorder;

    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Transactional(readOnly = true)
    public PageResponse<PromotionCampaignResponse> getPromotionCampaigns(PromotionCampaignSearchRequest request) {
        validateSearchRequest(request);

        Page<PromotionCampaign> page = promotionCampaignRepository.findAll(
                buildSpecification(request),
                PageRequest.of(request.getPage(), request.getSize(), buildSort(request))
        );

        return PageResponse.<PromotionCampaignResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PromotionCampaignResponse getPromotionCampaign(Long id) {
        return toResponse(findCampaign(id));
    }

    @Transactional
    public PromotionCampaignResponse createPromotionCampaign(PromotionCampaignRequest request) {
        validateCampaignRequest(request);

        String normalizedCode = normalizeCode(request.getCode());
        if (promotionCampaignRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new BadRequestException("Promotion campaign code already exists");
        }

        PromotionCampaign campaign = PromotionCampaign.builder().build();
        applyRequest(campaign, request, normalizedCode);

        PromotionCampaign savedCampaign = promotionCampaignRepository.save(campaign);
        PromotionCampaignResponse response = toResponse(savedCampaign);
        auditTrailRecorder.record(AuditActionType.PROMOTION_CAMPAIGN_CREATE, savedCampaign.getId(), null, response);
        return response;
    }

    @Transactional
    public PromotionCampaignResponse updatePromotionCampaign(Long id, PromotionCampaignRequest request) {
        validateCampaignRequest(request);

        PromotionCampaign campaign = findCampaign(id);
        PromotionCampaignResponse oldState = toResponse(campaign);
        String normalizedCode = normalizeCode(request.getCode());

        if (promotionCampaignRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new BadRequestException("Promotion campaign code already exists");
        }

        applyRequest(campaign, request, normalizedCode);
        PromotionCampaign savedCampaign = promotionCampaignRepository.save(campaign);
        PromotionCampaignResponse response = toResponse(savedCampaign);
        auditTrailRecorder.record(AuditActionType.PROMOTION_CAMPAIGN_UPDATE, savedCampaign.getId(), oldState, response);
        return response;
    }

    @Transactional
    public PromotionCampaignResponse updatePromotionCampaignStatus(Long id, boolean isActive) {
        PromotionCampaign campaign = findCampaign(id);
        PromotionCampaignResponse oldState = toResponse(campaign);

        campaign.setIsActive(isActive);
        PromotionCampaign savedCampaign = promotionCampaignRepository.save(campaign);
        PromotionCampaignResponse response = toResponse(savedCampaign);
        auditTrailRecorder.record(AuditActionType.PROMOTION_CAMPAIGN_STATUS_UPDATE, savedCampaign.getId(), oldState, response);
        return response;
    }

    private void applyRequest(PromotionCampaign campaign, PromotionCampaignRequest request, String normalizedCode) {
        campaign.setCode(normalizedCode);
        campaign.setName(normalizeRequiredText(request.getName(), "name"));
        campaign.setDescription(normalizeNullable(request.getDescription()));
        campaign.setStartAt(request.getStartAt());
        campaign.setEndAt(request.getEndAt());
        campaign.setTargetMemberLevel(request.getTargetMemberLevel());
        campaign.setConditionsJson(writeJson(request.getConditionsJson()));
        campaign.setRewardJson(writeJson(request.getRewardJson()));
        campaign.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
    }

    private PromotionCampaign findCampaign(Long id) {
        return promotionCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion campaign not found with id: " + id));
    }

    private void validateCampaignRequest(PromotionCampaignRequest request) {
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new BadRequestException("endAt must be after startAt");
        }
    }

    private void validateSearchRequest(PromotionCampaignSearchRequest request) {
        if (request.getStartsFrom() != null && request.getEndsTo() != null && request.getStartsFrom().isAfter(request.getEndsTo())) {
            throw new BadRequestException("startsFrom must be before or equal to endsTo");
        }
    }

    private Specification<PromotionCampaign> buildSpecification(PromotionCampaignSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String keyword = normalizeNullable(request.getKeyword());
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), likeKeyword),
                        cb.like(cb.lower(root.get("name")), likeKeyword)
                ));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            if (request.getTargetMemberLevel() != null) {
                predicates.add(cb.equal(root.get("targetMemberLevel"), request.getTargetMemberLevel()));
            }

            if (request.getStartsFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), request.getStartsFrom()));
            }

            if (request.getEndsTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endAt"), request.getEndsTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(PromotionCampaignSearchRequest request) {
        Sort.Direction direction = Sort.Direction.fromString(request.getSortDir());
        return Sort.by(direction, request.getSortBy());
    }

    private PromotionCampaignResponse toResponse(PromotionCampaign campaign) {
        return PromotionCampaignResponse.builder()
                .id(campaign.getId())
                .code(campaign.getCode())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .startAt(campaign.getStartAt())
                .endAt(campaign.getEndAt())
                .targetMemberLevel(campaign.getTargetMemberLevel())
                .conditionsJson(parseJson(campaign.getConditionsJson()))
                .rewardJson(parseJson(campaign.getRewardJson()))
                .isActive(campaign.getIsActive())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }

    private String normalizeCode(String value) {
        return normalizeRequiredText(value, "code").toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private String writeJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid JSON payload");
        }
    }

    private JsonNode parseJson(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        try {
            return jsonMapper.readTree(rawJson);
        } catch (Exception ex) {
            throw new BadRequestException("Stored JSON payload is invalid");
        }
    }
}
