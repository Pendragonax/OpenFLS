package de.vinz.openfls.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import java.util.function.Consumer

/** Forwards a compact representation of newly written log entries to admin clients. */
class LiveLogAppender : AppenderBase<ILoggingEvent>() {
    override fun append(event: ILoggingEvent) {
        @Suppress("UNCHECKED_CAST")
        val consumer = context.getObject(CONTEXT_KEY) as? Consumer<ILoggingEvent> ?: return
        runCatching { consumer.accept(event) }
            .onFailure { addError("Could not publish live log event", it) }
    }

    companion object {
        const val CONTEXT_KEY = "openfls.live-log-event-consumer"
    }
}
