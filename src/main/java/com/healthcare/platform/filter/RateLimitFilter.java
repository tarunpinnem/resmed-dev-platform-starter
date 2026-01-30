package com.healthcare.platform.filter;

import com.healthcare.platform.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Token Bucket algorithm.
 * Limits requests per client IP address.
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip rate limiting for health endpoints
        String path = httpRequest.getServletPath();
        if (isExcludedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String clientId = getClientIdentifier(httpRequest);
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);

        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            httpResponse.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            httpResponse.addHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));

            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for client: {}, path: {}", clientId, path);

            httpResponse.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            httpResponse.addHeader("X-RateLimit-Remaining", "0");
            httpResponse.addHeader("Retry-After", "60");

            throw new RateLimitExceededException(
                    "Rate limit exceeded. Maximum " + requestsPerMinute + " requests per minute allowed.");
        }
    }

    private Bucket createBucket(String clientId) {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get real IP from proxy headers
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/api/v1/health") ||
               path.startsWith("/api/v1/ready") ||
               path.startsWith("/api/v1/info") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}
