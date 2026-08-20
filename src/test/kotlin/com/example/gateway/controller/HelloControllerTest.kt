package com.example.gateway.controller // Defines package location for controller unit tests.

import com.example.gateway.service.EventPublisherService // Imports EventPublisherService to mock.
import org.junit.jupiter.api.Assertions.assertEquals // Assert equality helper.
import org.junit.jupiter.api.Test // JUnit 5 Test annotation.
import org.mockito.Mockito.mock // Mockito mock creation.
import org.mockito.kotlin.any // Mockito Kotlin null-safe any matcher.
import org.mockito.kotlin.anyOrNull // Mockito Kotlin null-safe anyOrNull matcher.
import org.mockito.kotlin.eq // Mockito Kotlin null-safe eq matcher.
import org.mockito.kotlin.verify // Mockito Kotlin verify function.
import org.slf4j.MDC // SLF4J MDC context.

class HelloControllerTest {

    private val eventPublisherService: EventPublisherService = mock(EventPublisherService::class.java) // Creates mock instance.
    private val helloController = HelloController(eventPublisherService) // Instantiates controller under test.

    @Test
    fun `hello endpoint triggers Kafka message and returns matching trace ID`() {
        val testCorrelationId = "custom-trace-id-9999" // Sample correlation trace ID.
        MDC.put("correlationId", testCorrelationId) // Sets correlation ID in MDC context.

        try {
            val response = helloController.hello() // Invokes GET /hello handler.

            assertEquals("Hello World trigger processed and EventHub Kafka message sent!", response["message"]) // Verifies response message text.
            assertEquals("gateway-requests", response["topic"]) // Verifies target topic name.
            assertEquals(testCorrelationId, response["traceId"]) // Verifies matching trace ID returned.

            // Verifies eventPublisherService.publishEvent was called with expected topic, payload, trace ID, and optional key.
            verify(eventPublisherService).publishEvent(eq("gateway-requests"), any(), eq(testCorrelationId), anyOrNull())
        } finally {
            MDC.remove("correlationId") // Clears MDC context after test.
        }
    }
}
