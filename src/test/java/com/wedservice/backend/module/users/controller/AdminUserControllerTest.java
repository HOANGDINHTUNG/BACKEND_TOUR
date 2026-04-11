package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.response.PageResponse;
import com.wedservice.backend.module.users.dto.request.AdminCreateUserRequest;
import com.wedservice.backend.module.users.dto.request.AdminUpdateUserRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.service.AdminUserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    void createUser_returnsWrappedApiResponse() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("a@example.com");
        request.setPasswordHash("123456");
        request.setPhone("0987654321");
        request.setRole(Role.CUSTOMER);

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.CUSTOMER)
                .build();

        when(adminUserService.createUser(any(AdminCreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.email").value("a@example.com"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    void getUsers_returnsPagedResponse() throws Exception {
        PageResponse<UserResponse> response = PageResponse.<UserResponse>builder()
                .content(List.of(
                        UserResponse.builder()
                                .id(UUID.randomUUID())
                                .fullName("Nguyen Van A")
                                .email("a@example.com")
                                .phone("0987654321")
                                .status(Status.ACTIVE)
                                .role(Role.CUSTOMER)
                                .build()
                ))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(adminUserService.getUsers(any())).thenReturn(response);

        mockMvc.perform(get("/users")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("sortBy", "createdAt")
                        .queryParam("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].email").value("a@example.com"));
    }


    @Test
    void getUserById_returnsWrappedApiResponse() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse response = UserResponse.builder()
                .id(id)
                .fullName("Detail User")
                .email("detail@example.com")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role(Role.ADMIN)
                .build();

        when(adminUserService.getUserById(id)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.email").value("detail@example.com"));
    }

    @Test
    void getUsers_returnsValidationErrors_whenPaginationOrSortIsInvalid() throws Exception {
        mockMvc.perform(get("/users")
                        .queryParam("page", "-1")
                        .queryParam("size", "0")
                        .queryParam("sortBy", "password")
                        .queryParam("sortDir", "down"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.page").exists())
                .andExpect(jsonPath("$.errors.size").exists())
                .andExpect(jsonPath("$.errors.sortBy").exists())
                .andExpect(jsonPath("$.errors.sortDir").exists());
    }

    @Test
    void updateUser_returnsWrappedApiResponse() throws Exception {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setFullName("Updated User");
        request.setEmail("updated@example.com");
        request.setPhone("0911222333");
        request.setRole(Role.ADMIN);

        UUID id = UUID.randomUUID();
        UserResponse response = UserResponse.builder()
                .id(id)
                .fullName("Updated User")
                .email("updated@example.com")
                .phone("0911222333")
                .status(Status.ACTIVE)
                .role(Role.ADMIN)
                .build();

        when(adminUserService.updateUser(any(UUID.class), any(AdminUpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated User"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void createUser_returnsValidationErrors_whenBodyIsInvalid() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setFullName("");
        request.setEmail("invalid-email");
        request.setPasswordHash("123");
        request.setPhone("");
        request.setRole(Role.MANAGER);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }
}
