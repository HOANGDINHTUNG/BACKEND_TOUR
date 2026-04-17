package com.wedservice.backend.module.schedulechat.dto.request;

import com.wedservice.backend.module.schedulechat.entity.ScheduleChatVisibility;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpsertScheduleChatRoomRequest {

    @Size(max = 200)
    private String roomName;

    private ScheduleChatVisibility visibility;

    private Boolean isActive;
}
