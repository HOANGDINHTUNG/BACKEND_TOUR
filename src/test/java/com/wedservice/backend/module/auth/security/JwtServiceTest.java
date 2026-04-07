package com.wedservice.backend.module.auth.security;

import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.Status;
import com.wedservice.backend.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-1234567890");
        jwtProperties.setExpiration(60_000L);
        jwtService = new JwtService(jwtProperties, new ObjectMapper());
    }

    @Test
    void generateAccessToken_extractsSubject_andValidatesSuccessfully() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .passwordHash("encoded")
                .phone("0123456789")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();

        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

        String token = jwtService.generateAccessToken(userDetails);

        assertThat(jwtService.extractSubject(token)).isEqualTo("test@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void extractAllClaims_throwsException_whenSignatureIsInvalid() {
        String invalidToken = "header.payload.signature";

        assertThatThrownBy(() -> jwtService.extractAllClaims(invalidToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void extractSubject_throwsException_whenTokenFormatIsInvalid() {
        assertThatThrownBy(() -> jwtService.extractSubject("not-a-jwt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JWT token format");
    }
}
