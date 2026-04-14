package com.wedservice.backend.module.users.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.service.UserProfileService;

import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserProfileService userProfileService;

    @Test
    void getMyProfile_returnsWrappedApiResponse() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Customer One")
                .email("customer@example.com")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role("CUSTOMER")
                .build();

        when(userProfileService.getMyProfile()).thenReturn(response);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Current user fetched successfully"))
                .andExpect(jsonPath("$.data.email").value("customer@example.com"));
    }

    @Test
    void updateMyProfile_returnsWrappedApiResponse() throws Exception {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName("Customer Updated");
        request.setEmail("customer.updated@example.com");
        request.setPhone("0987654321");

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Customer Updated")
                .email("customer.updated@example.com")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role("CUSTOMER")
                .build();

        when(userProfileService.updateMyProfile(any(UpdateMyProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.fullName").value("Customer Updated"));
    }

    @Test
    void updateMyProfile_returnsValidationErrors_whenBodyIsInvalid() throws Exception {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName("");
        request.setEmail("invalid-email");
        request.setPhone("");

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }
}
