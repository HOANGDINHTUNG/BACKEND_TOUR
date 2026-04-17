package com.wedservice.backend.module.schedulechat.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.bookings.entity.BookingStatus;
import com.wedservice.backend.module.bookings.repository.BookingRepository;
import com.wedservice.backend.module.notifications.entity.NotificationType;
import com.wedservice.backend.module.notifications.service.InternalNotificationService;
import com.wedservice.backend.module.schedulechat.dto.request.CreateScheduleChatMessageRequest;
import com.wedservice.backend.module.schedulechat.dto.request.UpsertScheduleChatRoomRequest;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatMemberResponse;
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
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.module.users.service.AuditActionType;
import com.wedservice.backend.module.users.service.AuditTrailRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleChatService {

    private static final Set<BookingStatus> CHAT_ELIGIBLE_BOOKING_STATUSES = EnumSet.of(
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN,
            BookingStatus.COMPLETED
    );

    private final ScheduleChatRoomRepository scheduleChatRoomRepository;
    private final ScheduleChatRoomMemberRepository scheduleChatRoomMemberRepository;
    private final ScheduleChatMessageRepository scheduleChatMessageRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditTrailRecorder auditTrailRecorder;
    private final InternalNotificationService internalNotificationService;

    @Transactional
    public ScheduleChatRoomResponse getMyRoom(Long scheduleId) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        ScheduleChatRoom room = resolveAccessibleRoom(scheduleId, userId, false);
        return toRoomResponse(room);
    }

    @Transactional(readOnly = true)
    public Page<ScheduleChatMessageResponse> getMyMessages(Long scheduleId, Pageable pageable) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        ScheduleChatRoom room = resolveAccessibleRoom(scheduleId, userId, false);
        return toMessageResponses(room.getId(), pageable);
    }

    @Transactional
    public ScheduleChatMessageResponse sendMyMessage(Long scheduleId, CreateScheduleChatMessageRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        ScheduleChatRoom room = resolveAccessibleRoom(scheduleId, userId, false);
        ScheduleChatRoomMember member = ensureMember(room.getId(), userId);
        ensureMemberCanSend(member);
        ScheduleChatMessage saved = scheduleChatMessageRepository.save(ScheduleChatMessage.builder()
                .roomId(room.getId())
                .senderUserId(userId)
                .messageText(normalizeRequiredMessage(request.getMessageText()))
                .attachmentUrl(normalizeNullable(request.getAttachmentUrl()))
                .build());

        // Notify other members (simple impl)
        notifyOtherMembers(room, userId, saved.getMessageText());

        return toMessageResponse(saved, resolveActiveUsers(Set.of(userId)).get(userId));
    }

    @Transactional
    public ScheduleChatRoomResponse getAdminRoom(Long scheduleId) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        ScheduleChatRoom room = resolveAccessibleRoom(scheduleId, userId, true);
        return toRoomResponse(room);
    }

    @Transactional
    public ScheduleChatRoomResponse upsertAdminRoom(Long scheduleId, UpsertScheduleChatRoomRequest request) {
        UUID actorUserId = authenticatedUserProvider.getRequiredCurrentUserId();
        TourSchedule schedule = resolveSchedule(scheduleId);
        ScheduleChatRoom room = scheduleChatRoomRepository.findByScheduleId(scheduleId)
                .orElseGet(() -> ScheduleChatRoom.builder()
                        .scheduleId(scheduleId)
                        .roomName(defaultRoomName(schedule))
                        .visibility(ScheduleChatVisibility.ALL_MEMBERS)
                        .isActive(Boolean.TRUE)
                        .build());

        ScheduleChatRoomResponse oldState = room.getId() == null ? null : toRoomResponse(room);
        String normalizedRoomName = normalizeNullable(request.getRoomName());
        if (StringUtils.hasText(normalizedRoomName)) {
            room.setRoomName(normalizedRoomName);
        } else if (!StringUtils.hasText(room.getRoomName())) {
            room.setRoomName(defaultRoomName(schedule));
        }
        if (request.getVisibility() != null) {
            room.setVisibility(request.getVisibility());
        }
        if (request.getIsActive() != null) {
            room.setIsActive(request.getIsActive());
        }

        ScheduleChatRoom saved = scheduleChatRoomRepository.save(room);
        syncEligibleBookedMembers(saved);
        ensureMember(saved.getId(), actorUserId);
        ScheduleChatRoomResponse response = toRoomResponse(saved);
        auditTrailRecorder.record(AuditActionType.SCHEDULE_CHAT_ROOM_UPSERT, saved.getId(), oldState, response);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<ScheduleChatMessageResponse> getAdminMessages(Long scheduleId, Pageable pageable) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        ScheduleChatRoom room = resolveAccessibleRoom(scheduleId, userId, true);
        return toMessageResponses(room.getId(), pageable);
    }

    @Transactional
    public ScheduleChatMessageResponse sendAdminMessage(Long scheduleId, CreateScheduleChatMessageRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        ScheduleChatRoom room = resolveAccessibleRoom(scheduleId, userId, true);
        ScheduleChatRoomMember member = ensureMember(room.getId(), userId);
        ensureMemberCanSend(member);
        ScheduleChatMessage saved = scheduleChatMessageRepository.save(ScheduleChatMessage.builder()
                .roomId(room.getId())
                .senderUserId(userId)
                .messageText(normalizeRequiredMessage(request.getMessageText()))
                .attachmentUrl(normalizeNullable(request.getAttachmentUrl()))
                .build());

        // Notify all members for admin message
        notifyOtherMembers(room, userId, saved.getMessageText());

        ScheduleChatMessageResponse response = toMessageResponse(saved, resolveActiveUsers(Set.of(userId)).get(userId));
        auditTrailRecorder.record(AuditActionType.SCHEDULE_CHAT_MESSAGE_SEND, saved.getId(), null, response);
        return response;
    }

    @Transactional
    public void muteMember(Long scheduleId, UUID userId, boolean muted) {
        ScheduleChatRoom room = scheduleChatRoomRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found for schedule: " + scheduleId));
        ScheduleChatRoomMember member = scheduleChatRoomMemberRepository.findByRoomIdAndUserId(room.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this chat room"));

        boolean oldMuted = Boolean.TRUE.equals(member.getIsMuted());
        member.setIsMuted(muted);
        scheduleChatRoomMemberRepository.save(member);

        auditTrailRecorder.record(AuditActionType.SCHEDULE_CHAT_MEMBER_MUTE_UPDATE, member.getId(), oldMuted, muted);
    }

    private ScheduleChatRoom resolveAccessibleRoom(Long scheduleId, UUID currentUserId, boolean backoffice) {
        TourSchedule schedule = resolveSchedule(scheduleId);
        ScheduleChatRoom room = scheduleChatRoomRepository.findByScheduleId(scheduleId)
                .orElseGet(() -> scheduleChatRoomRepository.save(ScheduleChatRoom.builder()
                        .scheduleId(scheduleId)
                        .roomName(defaultRoomName(schedule))
                        .visibility(ScheduleChatVisibility.ALL_MEMBERS)
                        .isActive(Boolean.TRUE)
                        .build()));

        if (!backoffice) {
            if (!Boolean.TRUE.equals(room.getIsActive())) {
                throw new BadRequestException("Schedule chat room is inactive");
            }
            if (room.getVisibility() == ScheduleChatVisibility.STAFF_ONLY) {
                throw new AccessDeniedException("You do not have permission to access this chat room");
            }
            if (!bookingRepository.existsByScheduleIdAndUserIdAndStatusIn(scheduleId, currentUserId, CHAT_ELIGIBLE_BOOKING_STATUSES)) {
                throw new AccessDeniedException("You do not have permission to access this chat room");
            }
        }

        syncEligibleBookedMembers(room);
        ensureMember(room.getId(), currentUserId);
        return room;
    }

    private TourSchedule resolveSchedule(Long scheduleId) {
        return tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour schedule not found with id: " + scheduleId));
    }

    private void syncEligibleBookedMembers(ScheduleChatRoom room) {
        List<UUID> eligibleUserIds = bookingRepository.findDistinctUserIdsByScheduleIdAndStatusIn(
                room.getScheduleId(),
                CHAT_ELIGIBLE_BOOKING_STATUSES
        );
        if (eligibleUserIds.isEmpty()) {
            return;
        }

        Set<UUID> existingUserIds = scheduleChatRoomMemberRepository.findByRoomIdOrderByJoinedAtAsc(room.getId()).stream()
                .map(ScheduleChatRoomMember::getUserId)
                .collect(Collectors.toSet());

        List<ScheduleChatRoomMember> membersToInsert = eligibleUserIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .map(userId -> ScheduleChatRoomMember.builder()
                        .roomId(room.getId())
                        .userId(userId)
                        .build())
                .toList();

        if (!membersToInsert.isEmpty()) {
            scheduleChatRoomMemberRepository.saveAll(membersToInsert);
        }
    }

    private ScheduleChatRoomMember ensureMember(Long roomId, UUID userId) {
        return scheduleChatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> scheduleChatRoomMemberRepository.save(ScheduleChatRoomMember.builder()
                        .roomId(roomId)
                        .userId(userId)
                        .build()));
    }

    private void ensureMemberCanSend(ScheduleChatRoomMember member) {
        if (Boolean.TRUE.equals(member.getIsMuted())) {
            throw new BadRequestException("Current user is muted in this chat room");
        }
    }

    private Page<ScheduleChatMessageResponse> toMessageResponses(Long roomId, Pageable pageable) {
        Page<ScheduleChatMessage> messages = scheduleChatMessageRepository.findByRoomId(roomId, pageable);
        Map<UUID, User> users = resolveActiveUsers(messages.getContent().stream()
                .map(ScheduleChatMessage::getSenderUserId)
                .collect(Collectors.toSet()));
        return messages.map(message -> toMessageResponse(message, users.get(message.getSenderUserId())));
    }

    private ScheduleChatRoomResponse toRoomResponse(ScheduleChatRoom room) {
        TourSchedule schedule = resolveSchedule(room.getScheduleId());
        List<ScheduleChatRoomMember> members = scheduleChatRoomMemberRepository.findByRoomIdOrderByJoinedAtAsc(room.getId());
        Map<UUID, User> users = resolveActiveUsers(members.stream()
                .map(ScheduleChatRoomMember::getUserId)
                .collect(Collectors.toSet()));

        List<ScheduleChatMemberResponse> memberResponses = members.stream()
                .map(member -> {
                    User user = users.get(member.getUserId());
                    return ScheduleChatMemberResponse.builder()
                            .id(member.getId())
                            .userId(member.getUserId())
                            .fullName(user == null ? null : user.getFullName())
                            .displayName(user == null ? null : user.getDisplayName())
                            .muted(member.getIsMuted())
                            .joinedAt(member.getJoinedAt())
                            .build();
                })
                .toList();

        return ScheduleChatRoomResponse.builder()
                .id(room.getId())
                .scheduleId(room.getScheduleId())
                .scheduleCode(schedule.getScheduleCode())
                .roomName(room.getRoomName())
                .visibility(room.getVisibility())
                .active(room.getIsActive())
                .createdAt(room.getCreatedAt())
                .memberCount(memberResponses.size())
                .members(memberResponses)
                .build();
    }

    private ScheduleChatMessageResponse toMessageResponse(ScheduleChatMessage message, User sender) {
        return ScheduleChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .senderUserId(message.getSenderUserId())
                .senderFullName(sender == null ? null : sender.getFullName())
                .senderDisplayName(sender == null ? null : sender.getDisplayName())
                .messageText(message.getMessageText())
                .attachmentUrl(message.getAttachmentUrl())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private Map<UUID, User> resolveActiveUsers(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, User> users = new HashMap<>();
        userRepository.findAllById(userIds).stream()
                .filter(user -> user.getDeletedAt() == null && user.getStatus() == Status.ACTIVE)
                .forEach(user -> users.put(user.getId(), user));
        return users;
    }

    private String defaultRoomName(TourSchedule schedule) {
        if (StringUtils.hasText(schedule.getScheduleCode())) {
            return schedule.getScheduleCode() + " chat";
        }
        return "Schedule " + schedule.getId() + " chat";
    }

    private void notifyOtherMembers(ScheduleChatRoom room, UUID senderUserId, String messageSnippet) {
        List<ScheduleChatRoomMember> members = scheduleChatRoomMemberRepository.findByRoomIdOrderByJoinedAtAsc(room.getId());
        String truncatedSnippet = messageSnippet.length() > 50 ? messageSnippet.substring(0, 47) + "..." : messageSnippet;
        
        members.stream()
                .filter(member -> !member.getUserId().equals(senderUserId))
                .forEach(member -> internalNotificationService.sendInAppNotification(
                        member.getUserId(),
                        NotificationType.CHAT,
                        "New message in " + room.getRoomName(),
                        truncatedSnippet,
                        "SCHEDULE_CHAT",
                        room.getId(),
                        null
                ));
    }

    private String normalizeRequiredMessage(String value) {
        String normalized = normalizeNullable(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BadRequestException("messageText is required");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = DataNormalizer.normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}
