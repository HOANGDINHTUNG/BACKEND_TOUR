package com.wedservice.backend.module.support.dto.response;

import com.wedservice.backend.module.support.entity.SupportSenderType;
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
public class SupportMessageResponse {

    private Long id;
    private Long sessionId;
    private SupportSenderType senderType;
    private UUID senderUserId;
    private String messageText;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}
