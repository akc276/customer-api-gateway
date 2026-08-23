package com.example.gateway.service

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EventPublisherService(
    private val kafkaTemplate: KafkaTemplate<String, String> // Autoconfigured Spring kafka helper
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventPublisherService::class.java) // SLF4J logger instance for EventPublisherService
    }

    fun publishEvent(topic: String, message: String, key: String? = null) {
        LOGGER.info("Publishing Kafka message to topic '{}' with key '{}'", topic, key) // Logs publishing event with automatic Micrometer trace context
        if (key != null) {
            kafkaTemplate.send(topic, key, message) // Sends message with partition key
        } else {
            kafkaTemplate.send(topic, message) // Sends message without key
        }
    }
}