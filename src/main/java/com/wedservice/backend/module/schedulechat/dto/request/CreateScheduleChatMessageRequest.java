package com.wedservice.backend.module.schedulechat.dto.request;

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
public class CreateScheduleChatMessageRequest {

    @Size(max = 5000)
    private String messageText;

    @Size(max = 2000)
    private String attachmentUrl;
}
