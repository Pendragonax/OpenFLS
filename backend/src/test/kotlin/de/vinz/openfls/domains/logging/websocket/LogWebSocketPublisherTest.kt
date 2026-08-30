package de.vinz.openfls.domains.logging.websocket

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import de.vinz.openfls.domains.logging.dto.LogEntryDto
import de.vinz.openfls.logback.LiveLogAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.util.function.Consumer

class LogWebSocketPublisherTest {

    private val context = LoggerFactory.getILoggerFactory() as LoggerContext

    private fun registeredConsumer(template: SimpMessagingTemplate): Consumer<ILoggingEvent> {
        LogWebSocketPublisher(template).registerLiveLogConsumer()
        @Suppress("UNCHECKED_CAST")
        return context.getObject(LiveLogAppender.CONTEXT_KEY) as Consumer<ILoggingEvent>
    }

    private fun event(message: String, throwable: Throwable?): ILoggingEvent =
        LoggingEvent("fqcn", context.getLogger("de.vinz.openfls.test"), Level.ERROR, message, throwable, emptyArray())

    @Test
    fun liveEntry_withThrowable_publishesRenderedStacktraceAndPlainMessage() {
        // Given
        val template = mock<SimpMessagingTemplate>()
        val consumer = registeredConsumer(template)

        // When
        consumer.accept(event("demo failed", RuntimeException("boom", IllegalStateException("root cause"))))

        // Then
        val captor = argumentCaptor<LogEntryDto>()
        verify(template).convertAndSend(eq(LogWebSocketTopic.LIVE_ENTRIES), captor.capture())
        val published = captor.firstValue
        assertThat(published.message).isEqualTo("demo failed")
        assertThat(published.stacktrace)
            .contains("java.lang.RuntimeException: boom")
            .contains("Caused by: java.lang.IllegalStateException: root cause")
    }

    @Test
    fun liveEntry_withoutThrowable_publishesNullStacktrace() {
        // Given
        val template = mock<SimpMessagingTemplate>()
        val consumer = registeredConsumer(template)

        // When
        consumer.accept(event("just an info", null))

        // Then
        val captor = argumentCaptor<LogEntryDto>()
        verify(template).convertAndSend(eq(LogWebSocketTopic.LIVE_ENTRIES), captor.capture())
        assertThat(captor.firstValue.stacktrace).isNull()
    }
}
