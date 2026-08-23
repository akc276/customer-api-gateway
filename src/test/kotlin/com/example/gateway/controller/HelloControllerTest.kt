package com.example.gateway.controller // Defines package location for controller unit tests.

import com.example.gateway.service.EventPublisherService // Imports EventPublisherService to mock.
import io.micrometer.tracing.Span // Micrometer Span model.
import io.micrometer.tracing.TraceContext // Micrometer TraceContext model.
import io.micrometer.tracing.Tracer // Micrometer Tracer model.
import org.junit.jupiter.api.Assertions.assertEquals // Assert equality helper.
import org.junit.jupiter.api.Test // JUnit 5 Test annotation.
import org.mockito.Mockito.mock // Mockito mock creation.
import org.mockito.kotlin.any // Mockito Kotlin null-safe any matcher.
import org.mockito.kotlin.anyOrNull // Mockito Kotlin null-safe anyOrNull matcher.
import org.mockito.kotlin.eq // Mockito Kotlin null-safe eq matcher.
import org.mockito.kotlin.verify // Mockito Kotlin verify function.
import org.mockito.kotlin.whenever // Mockito Kotlin whenever stubbing.

class HelloControllerTest {

    private val eventPublisherService: EventPublisherService = mock(EventPublisherService::class.java) // Creates mock instance for EventPublisherService.
    private val tracer: Tracer = mock(Tracer::class.java) // Creates mock instance for Tracer.
    private val helloController = HelloController(eventPublisherService, tracer) // Instantiates controller under test with mocked dependencies.

    @Test
    fun `hello endpoint triggers Kafka message and returns matching trace ID`() {
        val testTraceId = "4bf92f3577b34da6a3ce929d0e0e4736" // Sample 32-character W3C hex trace ID.
        val span: Span = mock(Span::class.java) // Mocks active Span.
        val traceContext: TraceContext = mock(TraceContext::class.java) // Mocks TraceContext.

        whenever(tracer.currentSpan()).thenReturn(span) // Stubs currentSpan call.
        whenever(span.context()).thenReturn(traceContext) // Stubs context call.
        whenever(traceContext.traceId()).thenReturn(testTraceId) // Stubs traceId call.

        val response = helloController.hello() // Invokes GET /hello handler.

        assertEquals("Hello World trigger processed and EventHub Kafka message sent!", response["message"]) // Verifies response message text.
        assertEquals("gateway-requests", response["topic"]) // Verifies target topic name.
        assertEquals(testTraceId, response["traceId"]) // Verifies matching W3C trace ID returned.

        // Verifies eventPublisherService.publishEvent was called with expected topic, payload, and optional key.
        verify(eventPublisherService).publishEvent(eq("gateway-requests"), any(), anyOrNull())
    }
}
