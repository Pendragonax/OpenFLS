package de.vinz.openfls.services

import de.vinz.openfls.logback.PerformanceLogbackFilter
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PerformanceLoggingService {

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    fun logPerformance(method: String, startMs: Long, logger: Logger) {
        if (logPerformance) {
            val elapsedMs = System.currentTimeMillis() - startMs
            logger.info("${PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING} $method took $elapsedMs ms")
        }
    }
}