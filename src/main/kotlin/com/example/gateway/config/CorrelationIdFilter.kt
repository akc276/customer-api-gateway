package com.example.gateway.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CorrelationIdFilter : Filter {

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
        const val CORRELATION_ID_LOG_VAR = "correlationId"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // Reuses incoming X-Correlation-ID header or generates a new UUID for the request trace
        val correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()

        try {
            // Injects correlationId and traceId into SLF4J Mapped Diagnostic Context (MDC) for log output
            MDC.put(CORRELATION_ID_LOG_VAR, correlationId)
            MDC.put("traceId", correlationId)

            // Echoes correlationId back in HTTP response header for client tracing
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId)

            chain.doFilter(request, response)
        } finally {
            MDC.remove(CORRELATION_ID_LOG_VAR) // Clears MDC after request finishes to prevent thread pollution
            MDC.remove("traceId")
        }
    }
}