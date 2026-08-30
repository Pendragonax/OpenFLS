package de.vinz.openfls.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class StructuredLogTest {

    private val slf4jLogger = LoggerFactory.getLogger("de.vinz.openfls.test.StructuredLogTest")
    private val logbackLogger = slf4jLogger as Logger
    private val appender = ListAppender<ILoggingEvent>()

    @BeforeEach
    fun setUp() {
        appender.start()
        logbackLogger.addAppender(appender)
    }

    @AfterEach
    fun tearDown() {
        logbackLogger.detachAppender(appender)
        appender.stop()
        MDC.clear()
    }

    @Test
    fun error_unexpectedException_logsAtErrorWithStacktraceAndRootCause() {
        // Given
        val root = IllegalStateException("db down")
        val exception = RuntimeException("wrapper failed", root)

        // When
        StructuredLog.error(slf4jLogger, "application.request.failed", exception)

        // Then
        assertThat(appender.list).hasSize(1)
        val event = appender.list.single()
        assertThat(event.level).isEqualTo(Level.ERROR)
        assertThat(event.throwableProxy).isNotNull
        assertThat(event.throwableProxy.className).isEqualTo("java.lang.RuntimeException")
        assertThat(event.formattedMessage)
            .contains("event_name=application.request.failed")
            .contains("outcome=failure")
            .contains("exception.type=java.lang.RuntimeException")
            .contains("exception.root_type=java.lang.IllegalStateException")
            .contains("exception.message=wrapper_failed")
    }

    @Test
    fun error_expectedIllegalArgumentException_logsAtWarnWithStacktrace() {
        // Given
        val exception = IllegalArgumentException("no permission to change the role")

        // When
        StructuredLog.error(slf4jLogger, "application.request.failed", exception)

        // Then
        val event = appender.list.single()
        assertThat(event.level).isEqualTo(Level.WARN)
        assertThat(event.throwableProxy).isNotNull
    }

    @Test
    fun error_projectInternalException_logsAtWarn() {
        // Given
        val exception = de.vinz.openfls.exceptions.IllegalTimeException("end before start")

        // When
        StructuredLog.error(slf4jLogger, "application.request.failed", exception)

        // Then
        assertThat(appender.list.single().level).isEqualTo(Level.WARN)
    }

    @Test
    fun error_expectedRootCauseInsideUnexpectedWrapper_logsAtWarn() {
        // Given
        val exception = RuntimeException("wrapper", IllegalArgumentException("bad input"))

        // When
        StructuredLog.error(slf4jLogger, "application.request.failed", exception)

        // Then
        assertThat(appender.list.single().level).isEqualTo(Level.WARN)
    }

    @Test
    fun error_exceptionMessageWithControlCharacters_isSanitizedAgainstLogInjection() {
        // Given
        val exception = RuntimeException("line1\nERROR forged-second-line\rwith\ttabs")

        // When
        StructuredLog.error(slf4jLogger, "evt", exception)

        // Then
        val rendered = appender.list.single().formattedMessage
        assertThat(rendered).doesNotContain("\n").doesNotContain("\r").doesNotContain("\t")
        assertThat(rendered).contains("exception.message=line1_ERROR_forged-second-line_with_tabs")
    }

    @Test
    fun error_nullExceptionMessage_rendersNonePlaceholder() {
        // Given
        val exception = NullPointerException()

        // When
        StructuredLog.error(slf4jLogger, "evt", exception)

        // Then
        assertThat(appender.list.single().formattedMessage).contains("exception.message=none")
    }

    @Test
    fun error_withRequestContextInMdc_includesCorrelationAndHttpFields() {
        // Given
        MDC.put(StructuredLog.MDC_CORRELATION_ID, "abc-123")
        MDC.put(StructuredLog.MDC_HTTP_METHOD, "POST")
        MDC.put(StructuredLog.MDC_HTTP_PATH, "/api/employees")

        // When
        StructuredLog.error(slf4jLogger, "evt", RuntimeException("x"))

        // Then
        assertThat(appender.list.single().formattedMessage)
            .contains("correlation_id=abc-123")
            .contains("http.method=POST")
            .contains("http.path=/api/employees")
    }
}
