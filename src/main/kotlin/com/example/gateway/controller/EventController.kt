package com.example.gateway.controller

import com.example.gateway.service.EventPublisherService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class EventController(
    private val eventPublisherService: EventPublisherService
) {
    @PostMapping("/publish")
    fun publish(@RequestBody payload: Map<String, String>): Map<String, String> {
        val message = payload["message"] ?: "Default Gateway event"
        eventPublisherService.publishEvent("gateway-events", message) // Pushes events to `gateway-events` hub
        return mapOf("status" to "QUEUED", "message" to message)

    }

}