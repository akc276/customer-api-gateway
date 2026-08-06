package com.example.gateway.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EventPublisherService(
    private val kafkaTemplate: KafkaTemplate<String, String> // Autoconfigured Spring kafka helper
) {
    fun publishEvent(topic: String, message: String) {
        kafkaTemplate.send(topic, message) // Asynchronously pushes message to the specified Event hub topic
    }
}