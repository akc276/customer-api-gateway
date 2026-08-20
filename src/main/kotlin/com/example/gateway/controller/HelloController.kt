package com.example.gateway.controller // Defines package location for REST endpoints.

import com.example.gateway.service.EventPublisherService // Imports EventPublisherService for publishing EventHub messages.
import org.slf4j.LoggerFactory // Correct SLF4J logger factory import for Kotlin logging.
import org.slf4j.MDC // MDC import for retrieving active trace ID.
import org.springframework.web.bind.annotation.GetMapping // Annotation mapping HTTP GET requests.
import org.springframework.web.bind.annotation.RestController // Marks class as a Spring REST controller component.

@RestController // Exposes controller endpoints returning JSON responses.
class HelloController( // Controller class handling diagnostic and hello world endpoints.
    private val eventPublisherService: EventPublisherService // Injects EventPublisherService bean.
) {

    companion object { // Holds static companion members shared across class instances.
        private val LOGGER = LoggerFactory.getLogger(HelloController::class.java) // Initializes SLF4J logger instance for HelloController.
    } // End of companion object block.

    @GetMapping("/hello") // Maps HTTP GET /hello endpoint requests.
    fun hello(): Map<String, String> { // Handler function returning key-value JSON response.
        val traceId = MDC.get("correlationId") ?: MDC.get("traceId") ?: "N/A" // Retrieves active trace ID from MDC context.
        LOGGER.info("Saying hello world and publishing EventHub message with traceId: {}", traceId) // Logs info message with traceId.
        
        val payload = "Hello World request payload generated at ${System.currentTimeMillis()}" // Builds sample event payload message string.
        eventPublisherService.publishEvent("gateway-requests", payload, traceId) // Publishes EventHub Kafka message to `gateway-requests` topic.
        
        return mapOf( // Returns JSON response details to HTTP client.
            "message" to "Hello World trigger processed and EventHub Kafka message sent!", // Informational response message.
            "topic" to "gateway-requests", // Target EventHub topic name.
            "traceId" to traceId // Current request trace ID.
        )
    } // End of hello function.
} // End of HelloController class.