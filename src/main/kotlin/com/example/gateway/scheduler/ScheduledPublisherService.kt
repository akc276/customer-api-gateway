package com.example.gateway.scheduler

import com.example.gateway.service.EventPublisherService
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledPublisherService(
    private val eventPublisherService: EventPublisherService,
    private val observationRegistry: ObservationRegistry // Injects Micrometer ObservationRegistry bean
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ScheduledPublisherService::class.java)
    }

    // Triggers immediately upon application startup
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        publishPeriodicHeartbeat()
    }

    // Spring Boot Cronjob: Runs every 5 minutes (at second 0 of every 5th minute)
    @Scheduled(cron = "0 */5 * * * *")
    fun publishPeriodicHeartbeat() {
        // Starts Micrometer Observation for the scheduled task execution
        Observation.createNotStarted("scheduled.heartbeat", observationRegistry).observe {
            LOGGER.info("Spring Boot cronjob triggered (every 5 mins): publishing heartbeat event to Kafka")
            val payload = "Periodic 5-minute cron heartbeat ping generated at ${System.currentTimeMillis()}"
            eventPublisherService.publishEvent("gateway-requests", payload, key = "heartbeat")
        }
    }
}
