package com.wedservice.backend.module.schedulechat.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.schedulechat.dto.request.CreateScheduleChatMessageRequest;
import com.wedservice.backend.module.schedulechat.dto.request.UpsertScheduleChatRoomRequest;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatMessageResponse;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatRoomResponse;
import com.wedservice.backend.module.schedulechat.entity.ScheduleChatMessage;
import com.wedservice.backend.module.schedulechat.entity.ScheduleChatRoom;
import com.wedservice.backend.module.schedulechat.entity.ScheduleChatRoomMember;
import com.wedservice.backend.module.schedulechat.entity.ScheduleChatVisibility;
import com.wedservice.backend.module.schedulechat.repository.ScheduleChatMessageRepository;
import com.wedservice.backend.module.schedulechat.repository.ScheduleChatRoomMemberRepository;
import com.wedservice.backend.module.schedulechat.repository.ScheduleChatRoomRepository;
import com.wedservice.backend.module.tours.entity.TourSchedule;
import com.wedservice.backend.module.tours.repository.TourScheduleRepository;
import com.wedservice.backend.module.notifications.service.InternalNotificationService;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleChatServiceTest {

    @Mock
    private ScheduleChatRoomRepository scheduleChatRoomRepository;
    @Mock
    private ScheduleChatRoomMemberRepository scheduleChatRoomMemberRepository;
    @Mock
    private ScheduleChatMessageRepository scheduleChatMessageRepository;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock
    private AuditTrailRecorder auditTrailRecorder;
    @Mock
    private InternalNotificationService internalNotificationService;

    private ScheduleChatService scheduleChatService;

    @BeforeEach
    void setUp() {
        scheduleChatService = new ScheduleChatService(
                scheduleChatRoomRepository,
                scheduleChatRoomMemberRepository,
                scheduleChatMessageRepository,
                tourScheduleRepository,
                bookingRepository,
                userRepository,
                authenticatedUserProvider,
                auditTrailRecorder,
                internalNotificationService
        );
    }

    @Test
    void getMyRoom_bootstrapsRoomAndMemberForEligibleBookingUser() {
        UUID userId = UUID.randomUUID();
        TourSchedule schedule = TourSchedule.builder()
                .id(10L)
                .scheduleCode("SCH10")
                .build();
        ScheduleChatRoom room = ScheduleChatRoom.builder()
                .id(5L)
                .scheduleId(10L)
                .roomName("SCH10 chat")
                .visibility(ScheduleChatVisibility.ALL_MEMBERS)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        ScheduleChatRoomMember member = ScheduleChatRoomMember.builder()
                .id(7L)
                .roomId(5L)
                .userId(userId)
                .joinedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        User user = User.builder()
                .id(userId)
                .fullName("Customer One")
                .displayName("customer1")
                .passwordHash("hash")
                .status(Status.ACTIVE)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(tourScheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(scheduleChatRoomRepository.findByScheduleId(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByScheduleIdAndUserIdAndStatusIn(eq(10L), eq(userId), any())).thenReturn(true);
        when(bookingRepository.findDistinctUserIdsByScheduleIdAndStatusIn(eq(10L), any())).thenReturn(List.of(userId));
        when(scheduleChatRoomMemberRepository.findByRoomIdOrderByJoinedAtAsc(5L)).thenReturn(List.of(member));
        when(scheduleChatRoomMemberRepository.findByRoomIdAndUserId(5L, userId)).thenReturn(Optional.of(member));
        when(userRepository.findAllById(any())).thenReturn(List.of(user));

        ScheduleChatRoomResponse response = scheduleChatService.getMyRoom(10L);

        assertEquals(5L, response.getId());
        assertEquals(1, response.getMemberCount());
        assertEquals("Customer One", response.getMembers().get(0).getFullName());
        verify(scheduleChatRoomRepository, never()).save(any(ScheduleChatRoom.class));
    }

    @Test
    void sendMyMessage_rejectsMutedMember() {
        UUID userId = UUID.randomUUID();
        TourSchedule schedule = TourSchedule.builder()
                .id(11L)
                .scheduleCode("SCH11")
                .build();
        ScheduleChatRoom room = ScheduleChatRoom.builder()
                .id(6L)
                .scheduleId(11L)
                .roomName("SCH11 chat")
                .visibility(ScheduleChatVisibility.ALL_MEMBERS)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        ScheduleChatRoomMember member = ScheduleChatRoomMember.builder()
                .id(8L)
                .roomId(6L)
                .userId(userId)
                .isMuted(true)
                .joinedAt(LocalDateTime.now())
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(userId);
        when(tourScheduleRepository.findById(11L)).thenReturn(Optional.of(schedule));
        when(scheduleChatRoomRepository.findByScheduleId(11L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByScheduleIdAndUserIdAndStatusIn(eq(11L), eq(userId), any())).thenReturn(true);
        when(bookingRepository.findDistinctUserIdsByScheduleIdAndStatusIn(eq(11L), any())).thenReturn(List.of(userId));
        when(scheduleChatRoomMemberRepository.findByRoomIdOrderByJoinedAtAsc(6L)).thenReturn(List.of(member));
        when(scheduleChatRoomMemberRepository.findByRoomIdAndUserId(6L, userId)).thenReturn(Optional.of(member));

        assertThrows(BadRequestException.class,
                () -> scheduleChatService.sendMyMessage(11L, CreateScheduleChatMessageRequest.builder()
                        .messageText("Hello")
                        .build()));
    }

    @Test
    void upsertAdminRoom_recordsAuditAndAppliesVisibility() {
        UUID adminId = UUID.randomUUID();
        TourSchedule schedule = TourSchedule.builder()
                .id(12L)
                .scheduleCode("SCH12")
                .build();
        ScheduleChatRoom existing = ScheduleChatRoom.builder()
                .id(9L)
                .scheduleId(12L)
                .roomName("Old room")
                .visibility(ScheduleChatVisibility.ALL_MEMBERS)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        ScheduleChatRoomMember member = ScheduleChatRoomMember.builder()
                .id(10L)
                .roomId(9L)
                .userId(adminId)
                .isMuted(false)
                .joinedAt(LocalDateTime.now())
                .build();
        User admin = User.builder()
                .id(adminId)
                .fullName("Operator")
                .displayName("ops")
                .passwordHash("hash")
                .status(Status.ACTIVE)
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(adminId);
        when(tourScheduleRepository.findById(12L)).thenReturn(Optional.of(schedule));
        when(scheduleChatRoomRepository.findByScheduleId(12L)).thenReturn(Optional.of(existing));
        when(scheduleChatRoomRepository.save(any(ScheduleChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.findDistinctUserIdsByScheduleIdAndStatusIn(eq(12L), any())).thenReturn(List.of());
        when(scheduleChatRoomMemberRepository.findByRoomIdOrderByJoinedAtAsc(9L)).thenReturn(List.of(member));
        when(scheduleChatRoomMemberRepository.findByRoomIdAndUserId(9L, adminId)).thenReturn(Optional.of(member));
        when(userRepository.findAllById(any())).thenReturn(List.of(admin));

        ScheduleChatRoomResponse response = scheduleChatService.upsertAdminRoom(12L, UpsertScheduleChatRoomRequest.builder()
                .roomName("Staff room")
                .visibility(ScheduleChatVisibility.STAFF_ONLY)
                .isActive(false)
                .build());

        assertEquals(ScheduleChatVisibility.STAFF_ONLY, response.getVisibility());
        assertEquals(false, response.getActive());
        verify(auditTrailRecorder).record(eq(AuditActionType.SCHEDULE_CHAT_ROOM_UPSERT), eq(9L), any(), any());
    }

    @Test
    void sendAdminMessage_recordsAudit() {
        UUID adminId = UUID.randomUUID();
        TourSchedule schedule = TourSchedule.builder()
                .id(13L)
                .scheduleCode("SCH13")
                .build();
        ScheduleChatRoom room = ScheduleChatRoom.builder()
                .id(13L)
                .scheduleId(13L)
                .roomName("SCH13 chat")
                .visibility(ScheduleChatVisibility.STAFF_ONLY)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        ScheduleChatRoomMember member = ScheduleChatRoomMember.builder()
                .id(11L)
                .roomId(13L)
                .userId(adminId)
                .isMuted(false)
                .joinedAt(LocalDateTime.now())
                .build();
        User admin = User.builder()
                .id(adminId)
                .fullName("Admin Sender")
                .displayName("admin")
                .passwordHash("hash")
                .status(Status.ACTIVE)
                .build();
        ScheduleChatMessage savedMessage = ScheduleChatMessage.builder()
                .id(20L)
                .roomId(13L)
                .senderUserId(adminId)
                .messageText("Please arrive early")
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticatedUserProvider.getRequiredCurrentUserId()).thenReturn(adminId);
        when(tourScheduleRepository.findById(13L)).thenReturn(Optional.of(schedule));
        when(scheduleChatRoomRepository.findByScheduleId(13L)).thenReturn(Optional.of(room));
        when(bookingRepository.findDistinctUserIdsByScheduleIdAndStatusIn(eq(13L), any())).thenReturn(List.of());
        when(scheduleChatRoomMemberRepository.findByRoomIdAndUserId(13L, adminId)).thenReturn(Optional.of(member));
        when(scheduleChatMessageRepository.save(any(ScheduleChatMessage.class))).thenReturn(savedMessage);
        when(userRepository.findAllById(any())).thenReturn(List.of(admin));

        ScheduleChatMessageResponse response = scheduleChatService.sendAdminMessage(13L, CreateScheduleChatMessageRequest.builder()
                .messageText("Please arrive early")
                .build());

        assertEquals("Please arrive early", response.getMessageText());
        verify(auditTrailRecorder).record(eq(AuditActionType.SCHEDULE_CHAT_MESSAGE_SEND), eq(20L), eq(null), any());
    }
}
