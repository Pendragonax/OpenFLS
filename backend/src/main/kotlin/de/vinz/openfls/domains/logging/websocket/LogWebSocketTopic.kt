package de.vinz.openfls.domains.logging.websocket

import de.vinz.openfls.domains.logging.dto.LogEntryDto
import de.vinz.openfls.logback.LiveLogAppender
import de.vinz.openfls.websocket.StompTopicAccessPolicy
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import jakarta.annotation.PostConstruct
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.function.Consumer

object LogWebSocketTopic {
    const val LIVE_ENTRIES = "/topic/admin/logs"
}

@Component
class LogWebSocketPublisher(private val messagingTemplate: SimpMessagingTemplate) {
    private val liveEventConsumer = Consumer<ILoggingEvent> { event ->
        publish(LogEntryDto(
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

    fun publish(entry: LogEntryDto) {
        messagingTemplate.convertAndSend(LogWebSocketTopic.LIVE_ENTRIES, entry)
    }
}

@Component
class LogWebSocketTopicAccessPolicy : StompTopicAccessPolicy {
    override val destination = LogWebSocketTopic.LIVE_ENTRIES
    override fun isAllowed(authentication: Authentication) = authentication.authorities.any { it.authority == "ADMIN" }
}
