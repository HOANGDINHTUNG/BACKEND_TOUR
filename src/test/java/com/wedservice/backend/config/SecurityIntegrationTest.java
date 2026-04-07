package com.wedservice.backend.config;

import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.Status;
import com.wedservice.backend.module.user.entity.User;
import com.wedservice.backend.module.user.repository.UserRepository;
import com.wedservice.backend.support.TestAuthenticationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;


    private User customerUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        customerUser = saveUser("Customer One", "customer@example.com", "0987654321", Role.CUSTOMER, Status.ACTIVE);
        targetUser = saveUser("Target User", "target@example.com", "0987654322", Role.CUSTOMER, Status.ACTIVE);
    }

    @Test
    void anonymous_canAccessHealthEndpoint() throws Exception {
        mockMvc.perform(get("/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void anonymous_cannotAccessMyProfile() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void customer_cannotAccessAdminUserList() throws Exception {
        mockMvc.perform(get("/users")
                        .with(TestAuthenticationFactory.customUser(customerUser.getId(), customerUser.getEmail(), Role.CUSTOMER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void admin_canAccessAdminUserList() throws Exception {
        mockMvc.perform(get("/users")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("sortBy", "createdAt")
                        .queryParam("sortDir", "desc")
                        .with(TestAuthenticationFactory.customUser(UUID.randomUUID(), "admin@example.com", Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void customer_canAccessOwnProfile() throws Exception {
        mockMvc.perform(get("/users/me")
                        .with(TestAuthenticationFactory.customUser(customerUser.getId(), customerUser.getEmail(), Role.CUSTOMER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("customer@example.com"));
    }

    @Test
    void admin_canDeactivateUser() throws Exception {
        mockMvc.perform(patch("/users/{id}/deactivate", targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestAuthenticationFactory.customUser(UUID.randomUUID(), "admin@example.com", Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"))
                .andExpect(jsonPath("$.data.deletedAt").exists());
    }

    @Test
    void customer_cannotDeactivateAnotherUser() throws Exception {
        mockMvc.perform(patch("/users/{id}/deactivate", targetUser.getId())
                        .with(TestAuthenticationFactory.customUser(customerUser.getId(), customerUser.getEmail(), Role.CUSTOMER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    private User saveUser(String fullName, String email, String phone, Role role, Status status) {
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash("encoded-password")
                .phone(phone)
                .status(status)
                .role(role)
                .build();
        return userRepository.save(user);
    }
}
