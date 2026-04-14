package com.wedservice.backend.support;

import java.util.UUID;

import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserRole;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Shared helper to build an authentication object backed by CustomUserDetails.
 */
public final class TestAuthenticationFactory {

    private TestAuthenticationFactory() {
    }

    public static RequestPostProcessor customUser(UUID userId, String email, String roleCode) {
        Role role = Role.builder()
                .code(roleCode)
                .name(roleCode.toLowerCase())
                .build();

        User user = User.builder()
                .id(userId)
                .fullName("Test User")
                .email(email)
                .passwordHash("encoded-password")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .build();

        user.getUserRoles().add(UserRole.builder()
                .user(user)
                .role(role)
                .isPrimary(true)
                .build());

        CustomUserDetails details = CustomUserDetails.fromUser(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        return authentication(auth);
    }
}
