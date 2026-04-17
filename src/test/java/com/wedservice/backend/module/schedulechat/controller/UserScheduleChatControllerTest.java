package com.wedservice.backend.module.schedulechat.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.schedulechat.dto.response.ScheduleChatRoomResponse;
import com.wedservice.backend.module.schedulechat.facade.UserScheduleChatFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserScheduleChatControllerTest {

    @Mock
    private UserScheduleChatFacade userScheduleChatFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserScheduleChatController(userScheduleChatFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyRoom_returnsWrappedApiResponse() throws Exception {
        when(userScheduleChatFacade.getMyRoom(15L)).thenReturn(ScheduleChatRoomResponse.builder()
                .id(2L)
                .scheduleId(15L)
                .roomName("SCH15 chat")
                .memberCount(2)
                .build());

        mockMvc.perform(get("/schedules/15/chat-room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Schedule chat room fetched successfully"))
                .andExpect(jsonPath("$.data.roomName").value("SCH15 chat"));
    }
}
