package com.wedservice.backend.module.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("encoded")
                .phone("0123456789")
                .active(true)
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
}
