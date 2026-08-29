package de.vinz.openfls.domains.logging.service

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

/**
 * Optional local development aid for validating log display and STOMP updates.
 * It is disabled by default and must never be enabled in a production deployment.
 */
@Component
@ConditionalOnProperty(prefix = "openfls.logging.demo", name = ["enabled"], havingValue = "true")
class LogDemoGenerator {
    private val logger = LoggerFactory.getLogger(LogDemoGenerator::class.java)
    private val sequence = AtomicInteger()

    @Scheduled(fixedRateString = "\${openfls.logging.demo.interval-ms}")
    fun createDemoEntry() {
        when (sequence.incrementAndGet() % 5) {
            0 -> logger.trace("Demo-Protokoll: detaillierter Trace-Eintrag")
            1 -> logger.debug("Demo-Protokoll: Debug-Eintrag")
            2 -> logger.info("Demo-Protokoll: Live-Aktualisierung erfolgreich")
            3 -> logger.warn("Demo-Protokoll: beispielhafte Warnung")
            else -> logger.error("Demo-Protokoll: beispielhafter Fehler")
        }
    }
}
