package com.wedservice.backend.common.security;

import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
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
    private static final Set<String> ADMIN_ROLE_CODES = Set.of("ADMIN", "SUPER_ADMIN");
    private static final Set<String> BACKOFFICE_ROLE_CODES = Set.of(
            "SUPER_ADMIN",
            "ADMIN",
            "OPERATOR",
            "FIELD_STAFF",
            "CONTENT_EDITOR"
    );

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
        return isCurrentUserInAnyRole(ADMIN_ROLE_CODES);
    }

    public boolean isCurrentUserBackoffice() {
        return isCurrentUserInAnyRole(BACKOFFICE_ROLE_CODES);
    }

    public boolean isCurrentUserInAnyRole(String... roles) {
        return isCurrentUserInAnyRole(Arrays.stream(roles).filter(role -> role != null && !role.isBlank()).toList());
    }

    public boolean isCurrentUserInAnyRole(Collection<String> roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        if (roles == null || roles.isEmpty()) {
            return false;
        }

        return details.getRoleCodes().stream().anyMatch(roles::contains);
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
