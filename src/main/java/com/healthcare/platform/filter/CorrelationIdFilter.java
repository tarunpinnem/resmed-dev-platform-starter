package com.healthcare.platform.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that manages correlation IDs for request tracing.
 * Ensures every request has a unique correlation ID for distributed tracing.
 */
@Component
@Order(0)
@Slf4j
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get or generate correlation ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = generateId();
        }

        // Always generate a new request ID for this specific request
        String requestId = generateId();

        // Set in MDC for logging
        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_REQUEST_ID, requestId);

        // Store in request attributes for access in controllers/services
        httpRequest.setAttribute("correlationId", correlationId);
        httpRequest.setAttribute("requestId", requestId);

        // Add to response headers
        httpResponse.addHeader(CORRELATION_ID_HEADER, correlationId);
        httpResponse.addHeader(REQUEST_ID_HEADER, requestId);

        try {
            log.debug("Processing request - correlationId: {}, requestId: {}, path: {}",
                    correlationId, requestId, httpRequest.getRequestURI());

            chain.doFilter(request, response);
        } finally {
            // Clear MDC to prevent memory leaks
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
