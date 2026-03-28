package com.wedservice.backend.module.user.controller;

import com.wedservice.backend.support.TestWebMvcConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedservice.backend.module.auth.security.JwtAuthenticationFilter;
import com.wedservice.backend.module.user.dto.request.CreateUserRequest;
import com.wedservice.backend.module.user.dto.response.UserResponse;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.service.UserService;
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

@WebMvcTest(UserController.class)
@Import(TestWebMvcConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createUser_returnsWrappedApiResponse() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("a@example.com");
        request.setPassword("123456");
        request.setPhone("0987654321");

        UserResponse response = UserResponse.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .phone("0987654321")
                .active(true)
                .role(Role.CUSTOMER)
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.email").value("a@example.com"));
    }
}