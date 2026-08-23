package com.example.gateway.config

import io.micrometer.tracing.Tracer
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class CorrelationIdFilter(
    private val tracer: Tracer // Injects Micrometer Tracer bean
) : Filter {

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // Retrieves active W3C traceId from Micrometer context or incoming header
        val traceId = tracer.currentSpan()?.context()?.traceId()
            ?: httpRequest.getHeader(CORRELATION_ID_HEADER)
            ?: "N/A"

        // Echoes traceId back in HTTP response header for client tracing
        httpResponse.setHeader(CORRELATION_ID_HEADER, traceId)

        chain.doFilter(request, response)
    }
}