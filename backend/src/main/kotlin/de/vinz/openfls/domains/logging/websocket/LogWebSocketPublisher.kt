package de.vinz.openfls.domains.logging.websocket

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import de.vinz.openfls.domains.logging.dto.LogEntryResponse
import de.vinz.openfls.logback.LiveLogAppender
import jakarta.annotation.PostConstruct
import java.time.Instant
import java.util.function.Consumer
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class LogWebSocketPublisher(private val messagingTemplate: SimpMessagingTemplate) {
    private val liveEventConsumer = Consumer<ILoggingEvent> { event ->
        publish(LogEntryResponse(
            timestamp = Instant.ofEpochMilli(event.timeStamp).toString(),
            level = event.level.levelStr,
            logger = event.loggerName,
            thread = event.threadName,
            message = event.formattedMessage,
            stacktrace = event.throwableProxy?.let(ThrowableProxyUtil::asString)
        ))
    }

    @PostConstruct
    fun registerLiveLogConsumer() {
        (LoggerFactory.getILoggerFactory() as LoggerContext)
            .putObject(LiveLogAppender.CONTEXT_KEY, liveEventConsumer)
    }

    fun publish(entry: LogEntryResponse) {
        messagingTemplate.convertAndSend(LogWebSocketTopic.LIVE_ENTRIES, entry)
    }
}
