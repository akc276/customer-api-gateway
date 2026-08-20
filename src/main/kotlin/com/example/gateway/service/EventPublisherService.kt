package com.example.gateway.service

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class EventPublisherService(
    private val kafkaTemplate: KafkaTemplate<String, String> // Autoconfigured Spring kafka helper
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventPublisherService::class.java) // SLF4J logger instance for EventPublisherService
    }

    fun publishEvent(topic: String, message: String, traceId: String? = null) {
        val currentTraceId = traceId ?: MDC.get("correlationId") ?: MDC.get("traceId") ?: "NO_TRACE_ID" // Resolves trace ID from argument or MDC context
        val record = ProducerRecord<String, String>(topic, message) // Constructs ProducerRecord for destination topic
        record.headers().add(RecordHeader("X-Correlation-ID", currentTraceId.toByteArray(StandardCharsets.UTF_8))) // Attaches X-Correlation-ID header
        record.headers().add(RecordHeader("traceId", currentTraceId.toByteArray(StandardCharsets.UTF_8))) // Attaches traceId header
        
        LOGGER.info("Publishing Kafka message to topic '{}' with traceId: {}", topic, currentTraceId) // Logs publishing event
        kafkaTemplate.send(record) // Sends enriched record to Event Hubs broker
    }
}