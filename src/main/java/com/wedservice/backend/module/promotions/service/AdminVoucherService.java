package com.wedservice.backend.module.promotions.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
import com.wedservice.backend.module.promotions.dto.request.VoucherRequest;
import com.wedservice.backend.module.promotions.dto.request.VoucherSearchRequest;
import com.wedservice.backend.module.promotions.dto.response.VoucherResponse;
import com.wedservice.backend.module.promotions.entity.PromotionCampaign;
import com.wedservice.backend.module.promotions.entity.Voucher;
import com.wedservice.backend.module.promotions.entity.VoucherApplicableScope;
import com.wedservice.backend.module.promotions.entity.VoucherDiscountType;
import com.wedservice.backend.module.promotions.repository.PromotionCampaignRepository;
import com.wedservice.backend.module.promotions.repository.VoucherRepository;
import com.wedservice.backend.module.tours.repository.TourRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminVoucherService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final VoucherRepository voucherRepository;
    private final PromotionCampaignRepository promotionCampaignRepository;
    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private final AuditTrailRecorder auditTrailRecorder;

    @Transactional(readOnly = true)
    public PageResponse<VoucherResponse> getVouchers(VoucherSearchRequest request) {
        validateSearchRequest(request);

        Page<Voucher> page = voucherRepository.findAll(
                buildSpecification(request),
                PageRequest.of(request.getPage(), request.getSize(), buildSort(request))
        );

        return PageResponse.<VoucherResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucher(Long id) {
        return toResponse(findVoucher(id));
    }

    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        validateVoucherRequest(request, null);

        String normalizedCode = normalizeCode(request.getCode());
        if (voucherRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new BadRequestException("Voucher code already exists");
        }

        Voucher voucher = Voucher.builder().build();
        applyRequest(voucher, request, normalizedCode);

        Voucher savedVoucher = voucherRepository.save(voucher);
        VoucherResponse response = toResponse(savedVoucher);
        auditTrailRecorder.record(AuditActionType.VOUCHER_CREATE, savedVoucher.getId(), null, response);
        return response;
    }

    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        Voucher voucher = findVoucher(id);
        validateVoucherRequest(request, voucher);

        String normalizedCode = normalizeCode(request.getCode());
        if (voucherRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new BadRequestException("Voucher code already exists");
        }

        VoucherResponse oldState = toResponse(voucher);
        applyRequest(voucher, request, normalizedCode);

        Voucher savedVoucher = voucherRepository.save(voucher);
        VoucherResponse response = toResponse(savedVoucher);
        auditTrailRecorder.record(AuditActionType.VOUCHER_UPDATE, savedVoucher.getId(), oldState, response);
        return response;
    }

    @Transactional
    public VoucherResponse updateVoucherStatus(Long id, boolean isActive) {
        Voucher voucher = findVoucher(id);
        VoucherResponse oldState = toResponse(voucher);

        voucher.setIsActive(isActive);
        Voucher savedVoucher = voucherRepository.save(voucher);
        VoucherResponse response = toResponse(savedVoucher);
        auditTrailRecorder.record(AuditActionType.VOUCHER_STATUS_UPDATE, savedVoucher.getId(), oldState, response);
        return response;
    }

    private void applyRequest(Voucher voucher, VoucherRequest request, String normalizedCode) {
        PromotionCampaign campaign = resolveCampaign(request.getCampaignId());
        validateScopeReferences(request.getApplicableScope(), request.getApplicableTourId(), request.getApplicableDestinationId());
        validateCampaignWindow(campaign, request.getStartAt(), request.getEndAt());

        voucher.setCode(normalizedCode);
        voucher.setCampaignId(campaign == null ? null : campaign.getId());
        voucher.setName(normalizeRequiredText(request.getName(), "name"));
        voucher.setDescription(normalizeNullable(request.getDescription()));
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setUsageLimitTotal(request.getUsageLimitTotal());
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser() == null ? 1 : request.getUsageLimitPerUser());
        voucher.setApplicableScope(request.getApplicableScope());
        voucher.setApplicableTourId(request.getApplicableScope() == VoucherApplicableScope.TOUR ? request.getApplicableTourId() : null);
        voucher.setApplicableDestinationId(
                request.getApplicableScope() == VoucherApplicableScope.DESTINATION ? request.getApplicableDestinationId() : null
        );
        voucher.setApplicableMemberLevel(request.getApplicableMemberLevel());
        voucher.setStartAt(request.getStartAt());
        voucher.setEndAt(request.getEndAt());
        voucher.setIsStackable(request.getIsStackable() == null ? false : request.getIsStackable());
        voucher.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
    }

    private Voucher findVoucher(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
    }

    private void validateVoucherRequest(VoucherRequest request, Voucher existingVoucher) {
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new BadRequestException("endAt must be after startAt");
        }

        if (request.getDiscountValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("discountValue must be >= 0");
        }

        if (request.getDiscountType() != VoucherDiscountType.GIFT && request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("discountValue must be > 0 for non-gift vouchers");
        }

        if (request.getDiscountType() == VoucherDiscountType.PERCENTAGE && request.getDiscountValue().compareTo(ONE_HUNDRED) > 0) {
            throw new BadRequestException("discountValue must be <= 100 for percentage vouchers");
        }

        if (request.getMaxDiscountAmount() != null && request.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("maxDiscountAmount must be > 0");
        }

        if (request.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("minOrderValue must be >= 0");
        }

        if (request.getUsageLimitTotal() != null && request.getUsageLimitTotal() <= 0) {
            throw new BadRequestException("usageLimitTotal must be >= 1");
        }

        if (request.getUsageLimitPerUser() != null && request.getUsageLimitPerUser() <= 0) {
            throw new BadRequestException("usageLimitPerUser must be >= 1");
        }

        if (existingVoucher != null
                && request.getUsageLimitTotal() != null
                && request.getUsageLimitTotal() < safeInteger(existingVoucher.getUsedCount())) {
            throw new BadRequestException("usageLimitTotal cannot be less than usedCount");
        }
    }

    private void validateSearchRequest(VoucherSearchRequest request) {
        if (request.getStartsFrom() != null && request.getEndsTo() != null && request.getStartsFrom().isAfter(request.getEndsTo())) {
            throw new BadRequestException("startsFrom must be before or equal to endsTo");
        }
    }

    private PromotionCampaign resolveCampaign(Long campaignId) {
        if (campaignId == null) {
            return null;
        }

        return promotionCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion campaign not found with id: " + campaignId));
    }

    private void validateCampaignWindow(PromotionCampaign campaign, LocalDateTime startAt, LocalDateTime endAt) {
        if (campaign == null) {
            return;
        }

        if (startAt.isBefore(campaign.getStartAt()) || endAt.isAfter(campaign.getEndAt())) {
            throw new BadRequestException("Voucher active window must stay within the campaign window");
        }
    }

    private void validateScopeReferences(VoucherApplicableScope scope, Long applicableTourId, Long applicableDestinationId) {
        if (scope == VoucherApplicableScope.ALL) {
            if (applicableTourId != null || applicableDestinationId != null) {
                throw new BadRequestException("applicableTourId and applicableDestinationId must be null when applicableScope is all");
            }
            return;
        }

        if (scope == VoucherApplicableScope.TOUR) {
            if (applicableTourId == null) {
                throw new BadRequestException("applicableTourId is required when applicableScope is tour");
            }
            if (applicableDestinationId != null) {
                throw new BadRequestException("applicableDestinationId must be null when applicableScope is tour");
            }
            tourRepository.findById(applicableTourId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + applicableTourId));
            return;
        }

        if (applicableDestinationId == null) {
            throw new BadRequestException("applicableDestinationId is required when applicableScope is destination");
        }
        if (applicableTourId != null) {
            throw new BadRequestException("applicableTourId must be null when applicableScope is destination");
        }
        destinationRepository.findById(applicableDestinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + applicableDestinationId));
    }

    private Specification<Voucher> buildSpecification(VoucherSearchRequest request) {
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

            if (request.getCampaignId() != null) {
                predicates.add(cb.equal(root.get("campaignId"), request.getCampaignId()));
            }

            if (request.getDiscountType() != null) {
                predicates.add(cb.equal(root.get("discountType"), request.getDiscountType()));
            }

            if (request.getApplicableScope() != null) {
                predicates.add(cb.equal(root.get("applicableScope"), request.getApplicableScope()));
            }

            if (request.getApplicableMemberLevel() != null) {
                predicates.add(cb.equal(root.get("applicableMemberLevel"), request.getApplicableMemberLevel()));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            if (request.getActiveAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), request.getActiveAt()));
                predicates.add(cb.greaterThanOrEqualTo(root.get("endAt"), request.getActiveAt()));
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

    private Sort buildSort(VoucherSearchRequest request) {
        return Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
    }

    private VoucherResponse toResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .campaignId(voucher.getCampaignId())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderValue(voucher.getMinOrderValue())
                .usageLimitTotal(voucher.getUsageLimitTotal())
                .usageLimitPerUser(voucher.getUsageLimitPerUser())
                .usedCount(voucher.getUsedCount())
                .applicableScope(voucher.getApplicableScope())
                .applicableTourId(voucher.getApplicableTourId())
                .applicableDestinationId(voucher.getApplicableDestinationId())
                .applicableMemberLevel(voucher.getApplicableMemberLevel())
                .startAt(voucher.getStartAt())
                .endAt(voucher.getEndAt())
                .isStackable(voucher.getIsStackable())
                .isActive(voucher.getIsActive())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
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

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }
}
