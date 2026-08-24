package com.example.gateway.scheduler

import com.example.gateway.service.EventPublisherService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class ScheduledPublisherServiceTest {

    private val eventPublisherService: EventPublisherService = mock(EventPublisherService::class.java)
    private val scheduledPublisherService = ScheduledPublisherService(eventPublisherService)

    @Test
    fun `publishPeriodicHeartbeat publishes heartbeat event with key heartbeat`() {
        scheduledPublisherService.publishPeriodicHeartbeat()

        verify(eventPublisherService).publishEvent(eq("gateway-requests"), any(), eq("heartbeat"))
    }
}
