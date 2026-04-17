package com.wedservice.backend.module.schedulechat.dto.response;

import com.wedservice.backend.module.schedulechat.entity.ScheduleChatVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleChatRoomResponse {

    private Long id;
    private Long scheduleId;
    private String scheduleCode;
    private String roomName;
    private ScheduleChatVisibility visibility;
    private Boolean active;
    private LocalDateTime createdAt;
    private Integer memberCount;
    private List<ScheduleChatMemberResponse> members;
}
