package com.wedservice.backend.module.support.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.support.dto.request.CreateSupportMessageRequest;
import com.wedservice.backend.module.support.dto.request.CreateSupportSessionRequest;
import com.wedservice.backend.module.support.entity.SupportMessage;
import com.wedservice.backend.module.support.entity.SupportSenderType;
import com.wedservice.backend.module.support.entity.SupportSession;
import com.wedservice.backend.module.support.entity.SupportSessionStatus;
import com.wedservice.backend.module.support.repository.SupportMessageRepository;
import com.wedservice.backend.module.support.repository.SupportSessionRepository;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UserSupportServiceTest {

    @Mock
    private SupportSessionRepository supportSessionRepository;
    @Mock
    private SupportMessageRepository supportMessageRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock
    private UserRepository userRepository;

    private UserSupportService userSupportService;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        userSupportService = new UserSupportService(
                supportSessionRepository,
                supportMessageRepository,
                authenticatedUserProvider,
                userRepository
        );
        currentUserId = UUID.randomUUID();
        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(currentUserId);
    }

    @Test
    void createMySupportSession_createsSessionAndInitialMessage() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(User.builder()
                .id(currentUserId)
                .fullName("Customer")
                .email("customer@example.com")
                .passwordHash("encoded")
                .phone("0900000000")
                .status(Status.ACTIVE)
                .build()));
        when(supportSessionRepository.save(any(SupportSession.class))).thenAnswer(invocation -> {
            SupportSession session = invocation.getArgument(0);
            session.setId(10L);
            session.setCreatedAt(LocalDateTime.of(2026, 4, 17, 10, 0));
            session.setUpdatedAt(LocalDateTime.of(2026, 4, 17, 10, 0));
            return session;
        });
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(invocation -> {
            SupportMessage message = invocation.getArgument(0);
            message.setId(15L);
            message.setCreatedAt(LocalDateTime.of(2026, 4, 17, 10, 1));
            return message;
        });

        var response = userSupportService.createMySupportSession(CreateSupportSessionRequest.builder()
                .initialMessage("  Toi can ho tro dat tour  ")
                .build());

        ArgumentCaptor<SupportSession> sessionCaptor = ArgumentCaptor.forClass(SupportSession.class);
        verify(supportSessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getStatus()).isEqualTo(SupportSessionStatus.OPEN);
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getMessages()).hasSize(1);
        assertThat(response.getMessages().get(0).getSenderType()).isEqualTo(SupportSenderType.CUSTOMER);
        assertThat(response.getMessages().get(0).getMessageText()).isEqualTo("Toi can ho tro dat tour");
    }

    @Test
    void sendMySupportMessage_movesSessionToWaitingStaff() {
        SupportSession session = SupportSession.builder()
                .id(20L)
                .userId(currentUserId)
                .status(SupportSessionStatus.WAITING_CUSTOMER)
                .build();
        when(supportSessionRepository.findByIdAndUserId(20L, currentUserId)).thenReturn(Optional.of(session));
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(invocation -> {
            SupportMessage message = invocation.getArgument(0);
            message.setId(30L);
            message.setCreatedAt(LocalDateTime.of(2026, 4, 17, 11, 0));
            return message;
        });

        var response = userSupportService.sendMySupportMessage(20L, CreateSupportMessageRequest.builder()
                .messageText("Xin cap nhat giup toi")
                .build());

        assertThat(response.getId()).isEqualTo(30L);
        assertThat(session.getStatus()).isEqualTo(SupportSessionStatus.WAITING_STAFF);
        verify(supportSessionRepository).save(eq(session));
    }

    @Test
    void sendMySupportMessage_rejectsClosedSession() {
        SupportSession session = SupportSession.builder()
                .id(20L)
                .userId(currentUserId)
                .status(SupportSessionStatus.CLOSED)
                .build();
        when(supportSessionRepository.findByIdAndUserId(20L, currentUserId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> userSupportService.sendMySupportMessage(20L, CreateSupportMessageRequest.builder()
                .messageText("xin chao")
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Support session is already closed");
    }

    @Test
    void getMySupportSession_throwsWhenMissing() {
        when(supportSessionRepository.findByIdAndUserId(99L, currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSupportService.getMySupportSession(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Support session not found with id: 99");
    }
}
