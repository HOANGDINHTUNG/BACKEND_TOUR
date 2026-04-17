package com.wedservice.backend.module.schedulechat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleChatMessageResponse {

    private Long id;
    private Long roomId;
    private UUID senderUserId;
    private String senderFullName;
    private String senderDisplayName;
    private String messageText;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}
