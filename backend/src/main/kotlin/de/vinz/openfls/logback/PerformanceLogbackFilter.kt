package de.vinz.openfls.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.filter.Filter

class PerformanceLogbackFilter : Filter<ILoggingEvent>() {

    override fun decide(event: ILoggingEvent): FilterReply {
        return if (event.message != null && event.message.contains(PERFORMANCE_FILTER_STRING)) {
            FilterReply.NEUTRAL
        } else FilterReply.DENY
    }

    companion object {
        const val PERFORMANCE_FILTER_STRING = "[performance-log]"
    }
}