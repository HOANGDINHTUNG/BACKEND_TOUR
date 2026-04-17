package com.wedservice.backend.module.loyalty.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.loyalty.dto.request.AdminBadgeRequest;
import com.wedservice.backend.module.loyalty.dto.request.UpdateBadgeStatusRequest;
import com.wedservice.backend.module.loyalty.entity.BadgeDefinition;
import com.wedservice.backend.module.loyalty.entity.PassportBadge;
import com.wedservice.backend.module.loyalty.entity.TravelPassport;
import com.wedservice.backend.module.loyalty.repository.BadgeDefinitionRepository;
import com.wedservice.backend.module.loyalty.repository.PassportBadgeRepository;
import com.wedservice.backend.module.loyalty.repository.TravelPassportRepository;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBadgeServiceTest {

    @Mock
    private BadgeDefinitionRepository badgeDefinitionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TravelPassportRepository travelPassportRepository;
    @Mock
    private PassportBadgeRepository passportBadgeRepository;
    @Mock
    private UserPassportService userPassportService;
    @Mock
    private AuditTrailRecorder auditTrailRecorder;

    private AdminBadgeService adminBadgeService;

    @BeforeEach
    void setUp() {
        adminBadgeService = new AdminBadgeService(
                badgeDefinitionRepository,
                userRepository,
                travelPassportRepository,
                passportBadgeRepository,
                userPassportService,
                auditTrailRecorder
        );
    }

    @Test
    void createBadge_normalizesCodeAndRecordsAudit() {
        when(badgeDefinitionRepository.existsByCodeIgnoreCase("FIRST_TRIP")).thenReturn(false);
        when(badgeDefinitionRepository.save(any(BadgeDefinition.class))).thenAnswer(invocation -> {
            BadgeDefinition badge = invocation.getArgument(0);
            badge.setId(1L);
            return badge;
        });

        var response = adminBadgeService.createBadge(AdminBadgeRequest.builder()
                .code(" first_trip ")
                .name(" First Trip ")
                .conditionJson("{\"minTours\":1}")
                .build());

        assertThat(response.getCode()).isEqualTo("FIRST_TRIP");
        assertThat(response.getName()).isEqualTo("First Trip");
        verify(auditTrailRecorder).record(any(), eq(1L), any(), any());
    }

    @Test
    void createBadge_rejectsInvalidConditionJson() {
        when(badgeDefinitionRepository.existsByCodeIgnoreCase("FIRST_TRIP")).thenReturn(false);

        assertThatThrownBy(() -> adminBadgeService.createBadge(AdminBadgeRequest.builder()
                .code("FIRST_TRIP")
                .name("First Trip")
                .conditionJson("{invalid")
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("conditionJson must be valid JSON");
    }

    @Test
    void grantBadge_isIdempotentWhenPassportBadgeAlreadyExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        TravelPassport passport = TravelPassport.builder()
                .id(10L)
                .userId(userId)
                .passportNo("TVP100")
                .build();
        BadgeDefinition badge = BadgeDefinition.builder()
                .id(20L)
                .code("FIRST_TRIP")
                .name("First Trip")
                .isActive(true)
                .build();
        PassportBadge existing = PassportBadge.builder()
                .id(30L)
                .passportId(10L)
                .badgeId(20L)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeDefinitionRepository.findById(20L)).thenReturn(Optional.of(badge));
        when(userPassportService.ensurePassport(user)).thenReturn(passport);
        when(passportBadgeRepository.findByPassportIdAndBadgeId(10L, 20L)).thenReturn(Optional.of(existing));

        var response = adminBadgeService.grantBadge(userId, 20L);

        assertThat(response.getPassportBadgeId()).isEqualTo(30L);
        assertThat(response.getBadgeCode()).isEqualTo("FIRST_TRIP");
    }

    @Test
    void updateBadgeStatus_updatesFlag() {
        BadgeDefinition badge = BadgeDefinition.builder()
                .id(8L)
                .code("CHECKIN_MASTER")
                .name("Checkin Master")
                .isActive(true)
                .build();
        when(badgeDefinitionRepository.findById(8L)).thenReturn(Optional.of(badge));
        when(badgeDefinitionRepository.save(any(BadgeDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = adminBadgeService.updateBadgeStatus(8L, UpdateBadgeStatusRequest.builder()
                .isActive(false)
                .build());

        assertThat(response.getIsActive()).isFalse();
    }
}
