package com.wedservice.backend.module.auth.controller;

import com.wedservice.backend.support.TestWebMvcConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.RegisterRequest;
import com.wedservice.backend.module.auth.security.JwtAuthenticationFilter;
import com.wedservice.backend.module.auth.service.AuthService;
import com.wedservice.backend.module.user.dto.response.UserResponse;
import com.wedservice.backend.module.user.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestWebMvcConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_returnsWrappedApiResponse() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("a@example.com");
        request.setPassword("123456");
        request.setPhone("0987654321");

        AuthResponse response = AuthResponse.builder()
                .accessToken("token")
                .tokenType("Bearer")
                .expiresIn(86_400_000L)
                .user(UserResponse.builder()
                        .id(1L)
                        .fullName("Nguyen Van A")
                        .email("a@example.com")
                        .phone("0987654321")
                        .active(true)
                        .role(Role.CUSTOMER)
                        .build())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("token"))
                .andExpect(jsonPath("$.data.user.email").value("a@example.com"));
    }
}