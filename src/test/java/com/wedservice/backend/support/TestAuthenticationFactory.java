package com.wedservice.backend.support;

import java.util.UUID;

import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Shared helper to build an authentication object backed by CustomUserDetails.
 *
 * <p>This is important because profile endpoints rely on a custom principal that
 * carries both email and userId. {@code @WithMockUser} only provides username
 * and roles, which is not enough for the phase 2 profile service.</p>
 */
public final class TestAuthenticationFactory {

    private TestAuthenticationFactory() {
    }

    public static RequestPostProcessor customUser(UUID userId, String email, Role role) {
        User user = User.builder()
                .id(userId)
                .fullName("Test User")
                .email(email)
                .passwordHash("encoded-password")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(role)
                .build();

        CustomUserDetails details = CustomUserDetails.fromUser(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        return authentication(auth);
    }
}
