package de.vinz.openfls

import de.vinz.openfls.logging.StructuredLog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @AfterEach
    fun tearDown() = MDC.clear()

    @Test
    fun handleUnhandled_returnsInternalServerErrorWithStableCodeAndCorrelationId() {
        // Given
        MDC.put(StructuredLog.MDC_CORRELATION_ID, "corr-9")

        // When
        val response = handler.handleUnhandled(RuntimeException("raw internal detail"))

        // Then
        assertThat(response.statusCode.value()).isEqualTo(500)
        val body = requireNotNull(response.body)
        assertThat(body.status).isEqualTo(500)
        assertThat(body.errorCode).isEqualTo("internal.error")
        assertThat(body.correlationId).isEqualTo("corr-9")
        assertThat(body.message).doesNotContain("raw internal detail")
    }

    @Test
    fun handleUnhandled_withoutCorrelationId_returnsNullCorrelation() {
        // When
        val response = handler.handleUnhandled(RuntimeException("boom"))

        // Then
        assertThat(requireNotNull(response.body).correlationId).isNull()
    }
}
