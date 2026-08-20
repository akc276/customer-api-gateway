package com.example.gateway.listener // Defines package for Kafka message listeners.

import org.apache.kafka.clients.consumer.ConsumerRecord // Kafka record class containing headers and message metadata.
import org.slf4j.LoggerFactory // SLF4J logging interface.
import org.slf4j.MDC // SLF4J Mapped Diagnostic Context for MDC log tracing.
import org.springframework.kafka.annotation.KafkaListener // Annotation marking listener method for Kafka topics.
import org.springframework.messaging.handler.annotation.Payload // Annotation binding event payload string.
import org.springframework.stereotype.Component // Spring component annotation.
import java.nio.charset.StandardCharsets // Standard character sets for decoding header bytes.
import java.util.UUID // Fallback trace ID generation.

@Component // Registers ResponseEventListener as a Spring bean.
class ResponseEventListener { // Component listening to response events produced by processor-service.

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ResponseEventListener::class.java) // Logger instance for ResponseEventListener.
    }

    @KafkaListener(topics = ["service-responses"], groupId = "gateway-cg") // Subscribes to `service-responses` topic using consumer group `gateway-cg`.
    fun listenResponse(
        @Payload message: String, // Injects payload body string.
        record: ConsumerRecord<String, String> // Injects low-level Kafka record object.
    ) {
        // Extracts X-Correlation-ID header byte array from Kafka record headers, fallback to traceId header or UUID.
        val headerBytes = record.headers().lastHeader("X-Correlation-ID")?.value()
            ?: record.headers().lastHeader("traceId")?.value()
        val traceId = headerBytes?.let { String(it, StandardCharsets.UTF_8) } ?: UUID.randomUUID().toString()

        try {
            MDC.put("correlationId", traceId) // Sets correlationId in SLF4J MDC context.
            MDC.put("traceId", traceId) // Sets traceId in SLF4J MDC context.

            LOGGER.info("[consumer-api-gateway] Received response message from processor-service. Payload: '{}'", message) // Logs response event with trace ID in MDC.
        } finally {
            MDC.remove("correlationId") // Clears MDC correlationId context.
            MDC.remove("traceId") // Clears MDC traceId context.
        }
    }
}
