package com.wedservice.backend.module.schedulechat.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.schedulechat.dto.request.UpsertScheduleChatRoomRequest;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatRoomResponse;
import com.wedservice.backend.module.schedulechat.entity.ScheduleChatVisibility;
import com.wedservice.backend.module.schedulechat.facade.AdminScheduleChatFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminScheduleChatControllerTest {

    @Mock
    private AdminScheduleChatFacade adminScheduleChatFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminScheduleChatController(adminScheduleChatFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void upsertRoom_returnsWrappedApiResponse() throws Exception {
        when(adminScheduleChatFacade.upsertRoom(eq(15L), any())).thenReturn(ScheduleChatRoomResponse.builder()
                .id(3L)
                .scheduleId(15L)
                .roomName("Staff room")
                .visibility(ScheduleChatVisibility.STAFF_ONLY)
                .build());

        mockMvc.perform(put("/admin/schedules/15/chat-room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpsertScheduleChatRoomRequest.builder()
                                .roomName("Staff room")
                                .visibility(ScheduleChatVisibility.STAFF_ONLY)
                                .isActive(true)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Schedule chat room updated successfully"))
                .andExpect(jsonPath("$.data.roomName").value("Staff room"));
    }
}
