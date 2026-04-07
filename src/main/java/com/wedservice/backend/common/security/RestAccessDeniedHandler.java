package com.wedservice.backend.common.security;

import com.wedservice.backend.common.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
// Dùng để set kiểu dữ liệu trả về vd : "application/json"
import org.springframework.http.MediaType;
// Exception xảy ra khi user bị từ chối vì không đủ quyền.
import org.springframework.security.access.AccessDeniedException;
// Đây là interface của Spring Security để xử lý lỗi 403 Forbidden.
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
// ObjectMapper là class của Jackson dùng để convert object Java thành JSON.
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("You do not have permission to access this resource")
                .errorCode("FORBIDDEN")
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
