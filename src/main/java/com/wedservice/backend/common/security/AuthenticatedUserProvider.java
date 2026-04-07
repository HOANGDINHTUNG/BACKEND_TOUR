package com.wedservice.backend.common.security;

import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.users.entity.Role;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Đây là một class Spring dùng để lấy thông tin user đang đăng nhập từ SecurityContextHolder của Spring Security.

/*
    Nói ngắn gọn, thay vì ở nhiều nơi trong code phải viết lại:
        SecurityContextHolder.getContext().getAuthentication()

    thì chỉ cần gọi:
        authenticatedUserProvider.getRequiredCurrentUserId();
        authenticatedUserProvider.getRequiredCurrentUserEmail();
 */

@Component
public class AuthenticatedUserProvider {
    // Lấy id từ context security
    public UUID getRequiredCurrentUserId() {
        Object principal = getRequiredPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getUserId();
        }
        throw new UnauthorizedException("Authenticated user id is not available");
    }

    // Lấy user hiện tại đang login từ context security
    public String getRequiredCurrentUserLogin() {
        Object principal = getRequiredPrincipal();

        return switch (principal) {
            case CustomUserDetails details -> details.getUsername();
            case UserDetails details -> details.getUsername();
            case String value when !value.isBlank() -> value;
            default -> throw new UnauthorizedException("Authenticated user email is not available");
        };
    }

    public String getRequiredCurrentUserEmailOrPhone() {
        return getRequiredCurrentUserLogin();
    }

    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getRole() == Role.ADMIN;
        }
        return false;
    }

    private Object getRequiredPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return principal;
    }
}
