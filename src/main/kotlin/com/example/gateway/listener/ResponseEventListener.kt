package com.example.gateway.listener // Defines package for Kafka message listeners.

import org.slf4j.LoggerFactory // SLF4J logging interface.
import org.springframework.kafka.annotation.KafkaListener // Annotation marking listener method for Kafka topics.
import org.springframework.messaging.handler.annotation.Payload // Annotation binding event payload string.
import org.springframework.stereotype.Component // Spring component annotation.

@Component // Registers ResponseEventListener as a Spring bean.
class ResponseEventListener { // Component listening to response events produced by processor-service.

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ResponseEventListener::class.java) // Logger instance for ResponseEventListener.
    }

    @KafkaListener(topics = ["service-responses"], groupId = "gateway-cg") // Subscribes to `service-responses` topic using consumer group `gateway-cg`.
    fun listenResponse(@Payload message: String) {
        LOGGER.info("[consumer-api-gateway] Received response message from processor-service. Payload: '{}'", message) // Logs response event with automatic trace context.
    }
}
