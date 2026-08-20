package com.example.gateway.controller // Defines package location for REST endpoints.

import org.springframework.web.bind.annotation.GetMapping // Annotation mapping HTTP GET requests.
import org.springframework.web.bind.annotation.RestController // Marks class as a Spring REST controller component.
import org.slf4j.LoggerFactory // Correct SLF4J logger factory import for Kotlin logging.

@RestController // Exposes controller endpoints returning JSON responses.
class HelloController { // Controller class handling basic diagnostic endpoints.

    companion object { // Holds static companion members shared across class instances.
        private val LOGGER = LoggerFactory.getLogger(HelloController::class.java) // Initializes SLF4J logger instance for HelloController.
    } // End of companion object block.

    @GetMapping("/hello") // Maps HTTP GET /hello endpoint requests.
    fun hello(): Map<String, String> { // Handler function returning key-value JSON response.
        LOGGER.info("saying hello") // Logs informational message with current MDC trace context.
        return mapOf("message" to "Hello World from devtools with logger!") // Returns response map serialized to JSON.
    } // End of hello function.
} // End of HelloController class.