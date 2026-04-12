package com.wedservice.backend.common.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * Integration tests for {@link CorrelationIdFilter}.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>When a client sends {@code X-Request-ID}, it is echoed back in the response.</li>
 *   <li>When no {@code X-Request-ID} is sent, the server generates one and returns it.</li>
 *   <li>MDC is cleaned up after each request (no leak into next request).</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorrelationIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String CORRELATION_HEADER = "X-Request-ID";
    // Use actuator/health as a simple publicly accessible endpoint
    private static final String HEALTH_URL = "/api/v1/actuator/health";

    @Test
    @DisplayName("Should echo back the X-Request-ID sent by client")
    void shouldEchoClientCorrelationId() throws Exception {
        String clientId = "my-custom-trace-id-12345";

        mockMvc.perform(get(HEALTH_URL).header(CORRELATION_HEADER, clientId))
                .andExpect(header().string(CORRELATION_HEADER, clientId));
    }

    @Test
    @DisplayName("Should generate X-Request-ID when client does not provide one")
    void shouldGenerateCorrelationIdWhenNotProvided() throws Exception {
        MvcResult result = mockMvc.perform(get(HEALTH_URL))
                .andExpect(header().exists(CORRELATION_HEADER))
                .andReturn();

        String generatedId = result.getResponse().getHeader(CORRELATION_HEADER);
        assertThat(generatedId).isNotBlank();
        // Should be a valid UUID format
        assertThat(generatedId).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        );
    }

    @Test
    @DisplayName("Should generate different IDs for each request when not provided")
    void shouldGenerateUniqueIdPerRequest() throws Exception {
        MvcResult result1 = mockMvc.perform(get(HEALTH_URL)).andReturn();
        MvcResult result2 = mockMvc.perform(get(HEALTH_URL)).andReturn();

        String id1 = result1.getResponse().getHeader(CORRELATION_HEADER);
        String id2 = result2.getResponse().getHeader(CORRELATION_HEADER);

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("MDC should be cleared after request completes (no thread leak)")
    void shouldClearMdcAfterRequest() throws Exception {
        mockMvc.perform(get(HEALTH_URL).header(CORRELATION_HEADER, "some-trace-id"))
                .andReturn();

        // After request is processed, MDC for the current thread should be cleared
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
