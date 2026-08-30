package de.vinz.openfls.logging

import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.slf4j.MDC
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestLoggingFilterTest {

    private val filter = RequestLoggingFilter()

    @AfterEach
    fun tearDown() = MDC.clear()

    @Test
    fun doFilter_generatesCorrelationId_setsHeader_andClearsMdcAfterwards() {
        // Given
        val request = MockHttpServletRequest("GET", "/api/employees")
        val response = MockHttpServletResponse()

        // When
        filter.doFilter(request, response, MockFilterChain())

        // Then
        assertThat(response.getHeader("X-Correlation-ID")).isNotBlank()
        assertThat(MDC.get(StructuredLog.MDC_CORRELATION_ID)).isNull()
        assertThat(MDC.get(StructuredLog.MDC_HTTP_METHOD)).isNull()
        assertThat(MDC.get(StructuredLog.MDC_HTTP_PATH)).isNull()
    }

    @Test
    fun doFilter_reusesValidInboundCorrelationId() {
        // Given
        val request = MockHttpServletRequest("POST", "/api/employees")
        request.addHeader("X-Correlation-ID", "inbound-corr-1234")
        val response = MockHttpServletResponse()

        // When
        filter.doFilter(request, response, MockFilterChain())

        // Then
        assertThat(response.getHeader("X-Correlation-ID")).isEqualTo("inbound-corr-1234")
    }

    @Test
    fun doFilter_rejectsMalformedInboundCorrelationId() {
        // Given
        val request = MockHttpServletRequest("POST", "/api/employees")
        request.addHeader("X-Correlation-ID", "short")
        val response = MockHttpServletResponse()

        // When
        filter.doFilter(request, response, MockFilterChain())

        // Then
        assertThat(response.getHeader("X-Correlation-ID")).isNotEqualTo("short")
        assertThat(response.getHeader("X-Correlation-ID")).isNotBlank()
    }

    @Test
    fun doFilter_skipsTheWebSocketHandshakeUnderTheContextPath() {
        // Given: STOMP endpoint "/ws" served under context path "/api". The filter
        // must not touch the response, otherwise the upgrade fails with HTTP 400.
        val handshake = MockHttpServletRequest("GET", "/api/ws")
        handshake.contextPath = "/api"
        val handshakeResponse = MockHttpServletResponse()
        val restCall = MockHttpServletRequest("GET", "/api/employees")
        restCall.contextPath = "/api"
        val restResponse = MockHttpServletResponse()

        // When
        filter.doFilter(handshake, handshakeResponse, MockFilterChain())
        filter.doFilter(restCall, restResponse, MockFilterChain())

        // Then: the handshake passed through untouched, the REST call was processed
        assertThat(handshakeResponse.getHeader("X-Correlation-ID")).isNull()
        assertThat(restResponse.getHeader("X-Correlation-ID")).isNotBlank()
    }

    @Test
    fun doFilter_whenChainThrows_propagatesExceptionAndStillClearsMdc() {
        // Given
        val request = MockHttpServletRequest("POST", "/api/employees")
        val response = MockHttpServletResponse()
        val chain = mock<FilterChain> {
            on { doFilter(any(), any()) } doThrow IllegalStateException("downstream boom")
        }

        // When / Then
        assertThatThrownBy { filter.doFilter(request, response, chain) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("downstream boom")
        assertThat(MDC.get(StructuredLog.MDC_CORRELATION_ID)).isNull()
        assertThat(MDC.get(StructuredLog.MDC_HTTP_METHOD)).isNull()
        assertThat(MDC.get(StructuredLog.MDC_HTTP_PATH)).isNull()
    }
}
