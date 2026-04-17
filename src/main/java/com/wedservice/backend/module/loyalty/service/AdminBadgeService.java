package com.wedservice.backend.module.loyalty.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.loyalty.dto.request.AdminBadgeRequest;
import com.wedservice.backend.module.loyalty.dto.request.UpdateBadgeStatusRequest;
import com.wedservice.backend.module.loyalty.dto.response.BadgeResponse;
import com.wedservice.backend.module.loyalty.dto.response.PassportBadgeResponse;
import com.wedservice.backend.module.loyalty.entity.BadgeDefinition;
import com.wedservice.backend.module.loyalty.entity.PassportBadge;
import com.wedservice.backend.module.loyalty.entity.TravelPassport;
import com.wedservice.backend.module.loyalty.repository.BadgeDefinitionRepository;
import com.wedservice.backend.module.loyalty.repository.PassportBadgeRepository;
import com.wedservice.backend.module.loyalty.repository.TravelPassportRepository;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminBadgeService {

    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final UserRepository userRepository;
    private final TravelPassportRepository travelPassportRepository;
    private final PassportBadgeRepository passportBadgeRepository;
    private final UserPassportService userPassportService;
    private final AuditTrailRecorder auditTrailRecorder;
    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges() {
        return badgeDefinitionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toBadgeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BadgeResponse getBadge(Long id) {
        return toBadgeResponse(findBadge(id));
    }

    @Transactional
    public BadgeResponse createBadge(AdminBadgeRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        validateBadgeRequest(request, normalizedCode, null);

        BadgeDefinition badge = BadgeDefinition.builder()
                .build();
        applyBadgeRequest(badge, request, normalizedCode);

        BadgeDefinition saved = badgeDefinitionRepository.save(badge);
        BadgeResponse response = toBadgeResponse(saved);
        auditTrailRecorder.record(AuditActionType.BADGE_CREATE, saved.getId(), null, response);
        return response;
    }

    @Transactional
    public BadgeResponse updateBadge(Long id, AdminBadgeRequest request) {
        BadgeDefinition badge = findBadge(id);
        BadgeResponse oldState = toBadgeResponse(badge);
        String normalizedCode = normalizeCode(request.getCode());
        validateBadgeRequest(request, normalizedCode, id);
        applyBadgeRequest(badge, request, normalizedCode);

        BadgeDefinition saved = badgeDefinitionRepository.save(badge);
        BadgeResponse response = toBadgeResponse(saved);
        auditTrailRecorder.record(AuditActionType.BADGE_UPDATE, saved.getId(), oldState, response);
        return response;
    }

    @Transactional
    public BadgeResponse updateBadgeStatus(Long id, UpdateBadgeStatusRequest request) {
        BadgeDefinition badge = findBadge(id);
        BadgeResponse oldState = toBadgeResponse(badge);
        badge.setIsActive(Boolean.TRUE.equals(request.getIsActive()));

        BadgeDefinition saved = badgeDefinitionRepository.save(badge);
        BadgeResponse response = toBadgeResponse(saved);
        auditTrailRecorder.record(AuditActionType.BADGE_STATUS_UPDATE, saved.getId(), oldState, response);
        return response;
    }

    @Transactional
    public PassportBadgeResponse grantBadge(UUID userId, Long badgeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        BadgeDefinition badge = findBadge(badgeId);
        if (!Boolean.TRUE.equals(badge.getIsActive())) {
            throw new BadRequestException("Badge is inactive");
        }

        TravelPassport passport = userPassportService.ensurePassport(user);
        PassportBadge existing = passportBadgeRepository.findByPassportIdAndBadgeId(passport.getId(), badge.getId()).orElse(null);
        if (existing != null) {
            return toPassportBadgeResponse(existing, badge);
        }

        PassportBadge passportBadge = passportBadgeRepository.save(PassportBadge.builder()
                .passportId(passport.getId())
                .badgeId(badge.getId())
                .build());

        PassportBadgeResponse response = toPassportBadgeResponse(passportBadge, badge);
        auditTrailRecorder.record(AuditActionType.PASSPORT_BADGE_GRANT, passportBadge.getId(), null, response);
        return response;
    }

    private BadgeDefinition findBadge(Long id) {
        return badgeDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with id: " + id));
    }

    private void applyBadgeRequest(BadgeDefinition badge, AdminBadgeRequest request, String normalizedCode) {
        badge.setCode(normalizedCode);
        badge.setName(request.getName().trim());
        badge.setDescription(normalizeNullable(request.getDescription()));
        badge.setIconUrl(normalizeNullable(request.getIconUrl()));
        badge.setConditionJson(normalizeNullable(request.getConditionJson()));
        badge.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());
    }

    private void validateBadgeRequest(AdminBadgeRequest request, String normalizedCode, Long currentId) {
        boolean duplicate = currentId == null
                ? badgeDefinitionRepository.existsByCodeIgnoreCase(normalizedCode)
                : badgeDefinitionRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, currentId);
        if (duplicate) {
            throw new BadRequestException("Badge code already exists");
        }
        validateJson(request.getConditionJson(), "conditionJson");
    }

    private String normalizeCode(String code) {
        String normalized = DataNormalizer.normalize(code);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private void validateJson(String rawJson, String fieldName) {
        if (!StringUtils.hasText(rawJson)) {
            return;
        }
        try {
            jsonMapper.readTree(rawJson);
        } catch (Exception ex) {
            throw new BadRequestException(fieldName + " must be valid JSON");
        }
    }

    private BadgeResponse toBadgeResponse(BadgeDefinition badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .code(badge.getCode())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .conditionJson(badge.getConditionJson())
                .isActive(badge.getIsActive())
                .createdAt(badge.getCreatedAt())
                .build();
    }

    private PassportBadgeResponse toPassportBadgeResponse(PassportBadge passportBadge, BadgeDefinition badge) {
        return PassportBadgeResponse.builder()
                .passportBadgeId(passportBadge.getId())
                .badgeId(badge.getId())
                .badgeCode(badge.getCode())
                .badgeName(badge.getName())
                .badgeDescription(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .conditionJson(badge.getConditionJson())
                .isActive(badge.getIsActive())
                .unlockedAt(passportBadge.getUnlockedAt())
                .build();
    }
}
