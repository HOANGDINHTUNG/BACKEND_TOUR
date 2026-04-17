package com.wedservice.backend.module.support.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.support.dto.request.AssignSupportSessionRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.UpdateSupportSessionStatusRequest;
import com.wedservice.backend.module.support.entity.SupportMessage;
import com.wedservice.backend.module.support.entity.SupportSession;
import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import com.wedservice.backend.module.support.repository.SupportMessageRepository;
import com.wedservice.backend.module.support.repository.SupportSessionRepository;
import com.wedservice.backend.module.notifications.service.InternalNotificationService;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserCategory;
import com.wedservice.backend.module.users.entity.UserRole;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSupportServiceTest {

    @Mock
    private SupportSessionRepository supportSessionRepository;
    @Mock
    private SupportMessageRepository supportMessageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock
    private AuditTrailRecorder auditTrailRecorder;
    @Mock
    private InternalNotificationService internalNotificationService;

    private AdminSupportService adminSupportService;

    @BeforeEach
    void setUp() {
        adminSupportService = new AdminSupportService(
                supportSessionRepository,
                supportMessageRepository,
                userRepository,
                authenticatedUserProvider,
                auditTrailRecorder,
                internalNotificationService
        );
    }

    @Test
    void assignSupportSession_acceptsActiveInternalStaff() {
        UUID staffId = UUID.randomUUID();
        SupportSession session = SupportSession.builder()
                .id(1L)
                .userId(UUID.randomUUID())
                .status(SupportSessionStatus.OPEN)
                .build();
        User staff = User.builder()
                .id(staffId)
                .fullName("Operator")
                .email("operator@example.com")
                .passwordHash("encoded")
                .phone("0900000001")
                .status(Status.ACTIVE)
                .userCategory(UserCategory.INTERNAL)
                .build();
        when(supportSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(supportSessionRepository.save(any(SupportSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = adminSupportService.assignSupportSession(1L, AssignSupportSessionRequest.builder()
                .assignedStaffId(staffId.toString())
                .build());

        assertThat(response.getAssignedStaffId()).isEqualTo(staffId);
        verify(auditTrailRecorder).record(eq(AuditActionType.SUPPORT_SESSION_ASSIGN), eq(1L), any(), any());
    }

    @Test
    void assignSupportSession_rejectsInvalidStaff() {
        UUID staffId = UUID.randomUUID();
        SupportSession session = SupportSession.builder().id(1L).build();
        User customer = User.builder()
                .id(staffId)
                .fullName("Customer")
                .email("customer@example.com")
                .passwordHash("encoded")
                .phone("0900000002")
                .status(Status.ACTIVE)
                .userCategory(UserCategory.CUSTOMER)
                .build();
        when(supportSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(staffId)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> adminSupportService.assignSupportSession(1L, AssignSupportSessionRequest.builder()
                .assignedStaffId(staffId.toString())
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("assignedStaffId must reference an active internal staff user");
    }

    @Test
    void updateSupportSessionStatus_setsEndedAtForResolvedSession() {
        SupportSession session = SupportSession.builder()
                .id(2L)
                .status(SupportSessionStatus.OPEN)
                .build();
        when(supportSessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(supportSessionRepository.save(any(SupportSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = adminSupportService.updateSupportSessionStatus(2L, UpdateSupportSessionStatusRequest.builder()
                .status(SupportSessionStatus.RESOLVED)
                .build());

        assertThat(response.getStatus()).isEqualTo(SupportSessionStatus.RESOLVED);
        assertThat(session.getEndedAt()).isNotNull();
        verify(auditTrailRecorder).record(eq(AuditActionType.SUPPORT_SESSION_STATUS_UPDATE), eq(2L), any(), any());
    }

    @Test
    void sendSupportReply_assignsCurrentStaffAndMovesToWaitingCustomer() {
        UUID staffId = UUID.randomUUID();
        SupportSession session = SupportSession.builder()
                .id(3L)
                .status(SupportSessionStatus.WAITING_STAFF)
                .build();
        User staff = User.builder()
                .id(staffId)
                .fullName("Operator")
                .email("operator@example.com")
                .passwordHash("encoded")
                .phone("0900000003")
                .status(Status.ACTIVE)
                .build();
        staff.getUserRoles().add(UserRole.builder()
                .user(staff)
                .role(Role.builder().code("OPERATOR").build())
                .isPrimary(true)
                .build());

        when(supportSessionRepository.findById(3L)).thenReturn(Optional.of(session));
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(staffId);
        when(userRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(invocation -> {
            SupportMessage message = invocation.getArgument(0);
            message.setId(99L);
            message.setCreatedAt(LocalDateTime.of(2026, 4, 17, 12, 0));
            return message;
        });

        var response = adminSupportService.sendSupportReply(3L, CreateSupportMessageRequest.builder()
                .messageText("Chung toi da tiep nhan")
                .build());

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(session.getAssignedStaffId()).isEqualTo(staffId);
        assertThat(session.getStatus()).isEqualTo(SupportSessionStatus.WAITING_CUSTOMER);
        verify(auditTrailRecorder).record(eq(AuditActionType.SUPPORT_MESSAGE_REPLY), eq(99L), eq(null), any());
    }
}
