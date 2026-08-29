package de.vinz.openfls.logging

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/** Records application readiness without exposing configuration values. */
@Component
class ApplicationLifecycleLogger {
    private val logger = LoggerFactory.getLogger(ApplicationLifecycleLogger::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun applicationReady() {
        logger.info("event_name=application.ready outcome=success")
    }
}
