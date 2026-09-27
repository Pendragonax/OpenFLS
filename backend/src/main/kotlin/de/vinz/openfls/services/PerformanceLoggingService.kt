package de.vinz.openfls.services

import de.vinz.openfls.logback.PerformanceLogbackFilter
import org.slf4j.Logger
import org.springframework.stereotype.Service

@Service
class PerformanceLoggingService {

    fun logPerformance(method: String, startMs: Long, logger: Logger) {
        val elapsedMs = System.currentTimeMillis() - startMs
        logger.debug("${PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING} $method took $elapsedMs ms")
    }
}
