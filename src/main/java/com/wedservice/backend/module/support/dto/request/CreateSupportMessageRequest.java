package com.wedservice.backend.module.support.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CreateSupportMessageRequest {

    @NotBlank
    private String messageText;

    @Size(max = 2000, message = "attachmentUrl must not exceed 2000 characters")
    private String attachmentUrl;
}
