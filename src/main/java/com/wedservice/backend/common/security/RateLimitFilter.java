package com.wedservice.backend.common.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Filter for Auth endpoints using Bucket4j.
 * Protects against brute-force attacks on /register and /login.
 * Applies per-IP limiting: 10 requests per 1 minute.
 * In production, replace in-memory bucket store with Redis.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    // In-memory per-IP bucket store. Replace with Redis-backed store for multi-instance deployments.
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(CAPACITY).refillGreedy(CAPACITY, REFILL_PERIOD))
                .build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        String path = request.getRequestURI();

        // Only apply rate limit to sensitive auth endpoints
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            String clientIp = resolveClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (!probe.isConsumed()) {
                long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
                sendRateLimitResponse(response, retryAfterSeconds);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendRateLimitResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        String body = "{\"success\":false,\"status\":429,\"message\":\"Too many requests. Please try again in "
                + retryAfterSeconds + " second(s).\"}";
        response.getWriter().write(body);
    }

    /**
     * Resolves the real client IP, respecting X-Forwarded-For for reverse-proxy environments.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For may contain a comma-separated list; the leftmost is the client IP
        return xfHeader.split(",")[0].trim();
    }
}
