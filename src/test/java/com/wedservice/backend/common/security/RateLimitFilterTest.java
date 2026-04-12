package com.wedservice.backend.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link RateLimitFilter}.
 *
 * <p>Each test uses a unique X-Forwarded-For IP so the in-memory bucket per IP
 * does not bleed across test methods running in the same Spring context.</p>
 *
 * <p>MockMvc with @SpringBootTest does NOT include context-path (/api/v1) in URLs —
 * use endpoint paths directly (e.g. /auth/login, not /api/v1/auth/login).</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    // MockMvc does NOT use the configured context-path — paths start at the servlet root.
    private static final String LOGIN_URL    = "/auth/login";
    private static final String REGISTER_URL = "/auth/register";
    private static final int RATE_LIMIT = 10;

    // ------------------------------------------------------------------ //
    //  Test 1: Request under threshold should NOT be 429
    //  IP: 10.0.0.1 — isolated from other tests
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Should allow requests under the rate limit threshold")
    void shouldAllowRequestsUnderThreshold() throws Exception {
        String isolatedIp = "10.0.0.1";

        // Just 1 request — well under the limit of 10
        mockMvc.perform(post(LOGIN_URL)
                        .header("X-Forwarded-For", isolatedIp)
                        .contentType("application/json")
                        .content("{\"login\":\"test@test.com\",\"passwordHash\":\"testpass\"}"))
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertNotEquals(
                                HttpStatus.TOO_MANY_REQUESTS.value(),
                                result.getResponse().getStatus()
                        )
                );
    }

    // ------------------------------------------------------------------ //
    //  Test 2: Exceed limit on /auth/login
    //  IP: 10.0.0.2 — isolated from other tests
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Should return 429 after exceeding rate limit on /auth/login")
    void shouldReturn429AfterExceedingRateLimitOnLogin() throws Exception {
        String isolatedIp = "10.0.0.2";

        // Exhaust the bucket by sending RATE_LIMIT requests (tokens: 10 → 0)
        for (int i = 0; i < RATE_LIMIT; i++) {
            mockMvc.perform(post(LOGIN_URL)
                    .header("X-Forwarded-For", isolatedIp)
                    .contentType("application/json")
                    .content("{\"login\":\"brute@test.com\",\"passwordHash\":\"wrong\"}"));
        }

        // The 11th request should be rate-limited (no tokens left)
        mockMvc.perform(post(LOGIN_URL)
                        .header("X-Forwarded-For", isolatedIp)
                        .contentType("application/json")
                        .content("{\"login\":\"brute@test.com\",\"passwordHash\":\"wrong\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(429));
    }

    // ------------------------------------------------------------------ //
    //  Test 3: Exceed limit on /auth/register
    //  IP: 10.0.0.3 — isolated from other tests
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Should return 429 after exceeding rate limit on /auth/register")
    void shouldReturn429AfterExceedingRateLimitOnRegister() throws Exception {
        String isolatedIp = "10.0.0.3";

        for (int i = 0; i < RATE_LIMIT; i++) {
            mockMvc.perform(post(REGISTER_URL)
                    .header("X-Forwarded-For", isolatedIp)
                    .contentType("application/json")
                    .content("{\"email\":\"spam@test.com\",\"passwordHash\":\"Test1234@\",\"fullName\":\"Test\",\"phone\":\"0123456789\"}"));
        }

        mockMvc.perform(post(REGISTER_URL)
                        .header("X-Forwarded-For", isolatedIp)
                        .contentType("application/json")
                        .content("{\"email\":\"spam@test.com\",\"passwordHash\":\"Test1234@\",\"fullName\":\"Test\",\"phone\":\"0123456789\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------ //
    //  Test 4: Non-auth endpoints should NEVER be rate-limited
    //  IP: 10.0.0.4 — isolated from other tests
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Should NOT apply rate limit to non-auth endpoints")
    void shouldNotApplyRateLimitToOtherEndpoints() throws Exception {
        String isolatedIp = "10.0.0.4";

        // Hit /system/health 16 times (well above the rate limit threshold)
        // It should never return 429 because RateLimitFilter only applies to /auth/**
        for (int i = 0; i <= RATE_LIMIT + 5; i++) {
            mockMvc.perform(get("/system/health")
                            .header("X-Forwarded-For", isolatedIp))
                    .andExpect(result ->
                            org.junit.jupiter.api.Assertions.assertNotEquals(
                                    HttpStatus.TOO_MANY_REQUESTS.value(),
                                    result.getResponse().getStatus()
                            )
                    );
        }
    }
}
