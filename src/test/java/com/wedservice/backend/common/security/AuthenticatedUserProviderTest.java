package com.wedservice.backend.common.security;

import com.wedservice.backend.common.exception.UnauthorizedException;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.entity.UserRole;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserProviderTest {

    private final AuthenticatedUserProvider authenticatedUserProvider = new AuthenticatedUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUserIdAndEmail_whenPrincipalIsCustomUserDetails() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .fullName("Current User")
                .email("current@example.com")
                .passwordHash("encoded")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .build();
        
        user.getUserRoles().add(UserRole.builder()
                .user(user)
                .role(Role.builder().code("CUSTOMER").build())
                .isPrimary(true)
                .build());

        CustomUserDetails details = CustomUserDetails.fromUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );

        assertThat(authenticatedUserProvider.getRequiredCurrentUserId()).isEqualTo(id);
        assertThat(authenticatedUserProvider.getRequiredCurrentUserLogin()).isEqualTo("current@example.com");
    }

    @Test
    void returnsEmail_whenPrincipalIsGenericUserDetails() {
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername("generic@example.com")
                .password("encoded")
                .authorities("ROLE_ADMIN")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );

        assertThat(authenticatedUserProvider.getRequiredCurrentUserLogin()).isEqualTo("generic@example.com");
        assertThatThrownBy(authenticatedUserProvider::getRequiredCurrentUserId)
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Authenticated user id is not available");
    }

    @Test
    void throwsUnauthorized_whenNoAuthenticationExists() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(authenticatedUserProvider::getRequiredCurrentUserLogin)
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");
    }
}
