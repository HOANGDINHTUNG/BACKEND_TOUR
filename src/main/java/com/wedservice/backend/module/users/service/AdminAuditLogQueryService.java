package com.wedservice.backend.module.users.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.users.dto.request.AuditLogSearchRequest;
import com.wedservice.backend.module.users.dto.response.AuditLogResponse;
import com.wedservice.backend.module.users.entity.AuditLog;
import com.wedservice.backend.module.users.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuditLogQueryService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(AuditLogSearchRequest request) {
        validateDateRange(request.getFrom(), request.getTo());

        Page<AuditLog> page = auditLogRepository.findAll(
                buildSpecification(request),
                PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return PageResponse.<AuditLogResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private Specification<AuditLog> buildSpecification(AuditLogSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getActorUserId() != null) {
                predicates.add(cb.equal(root.get("actorUserId"), request.getActorUserId()));
            }

            String actionName = DataNormalizer.normalize(request.getActionName());
            if (StringUtils.hasText(actionName)) {
                predicates.add(cb.like(cb.lower(root.get("actionName")), "%" + actionName.toLowerCase() + "%"));
            }

            String entityName = DataNormalizer.normalize(request.getEntityName());
            if (StringUtils.hasText(entityName)) {
                predicates.add(cb.like(cb.lower(root.get("entityName")), "%" + entityName.toLowerCase() + "%"));
            }

            String entityId = DataNormalizer.normalize(request.getEntityId());
            if (StringUtils.hasText(entityId)) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }

            if (request.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getFrom()));
            }

            if (request.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from must be before or equal to to");
        }
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorUserId(log.getActorUserId())
                .actionName(log.getActionName())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .oldData(auditLogService.parseJson(log.getOldData()))
                .newData(auditLogService.parseJson(log.getNewData()))
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
