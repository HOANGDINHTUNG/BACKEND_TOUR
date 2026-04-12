package com.wedservice.backend.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to handle correlation IDs (trace IDs) for distributed request tracing.
 *
 * <p>On every incoming request:</p>
 * <ul>
 *   <li>Extracts or generates a unique {@code X-Request-ID} header value.</li>
 *   <li>Injects it into SLF4J MDC under the key {@code traceId}, making it
 *       available in all log statements for that request thread.</li>
 *   <li>Echoes the ID back to the caller via the {@code X-Request-ID} response header
 *       so clients can correlate their logs with server-side logs.</li>
 * </ul>
 *
 * <p>The MDC is always cleaned up in a {@code finally} block to prevent
 * memory leaks in thread-pool environments.</p>
 *
 * <p>Ordered at {@link Ordered#HIGHEST_PRECEDENCE} so the trace ID is available
 * before any other filter runs.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Request-ID";
    public static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clean MDC to prevent leaks across thread-pool reuse
            MDC.remove(MDC_KEY);
        }
    }
}
