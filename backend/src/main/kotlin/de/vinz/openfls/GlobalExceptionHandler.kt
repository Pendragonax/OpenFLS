package de.vinz.openfls

import de.vinz.openfls.logging.StructuredLog
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Last-resort handler for exceptions that neither a controller's own `try/catch`
 * nor a domain-specific `@ControllerAdvice` dealt with.
 *
 * Runs at [Ordered.LOWEST_PRECEDENCE] so every more specific handler wins. It logs
 * the failure once, with stacktrace and correlation id, and returns a stable,
 * correlatable body instead of leaking the raw exception message to the client.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Throwable::class)
    fun handleUnhandled(ex: Throwable): ResponseEntity<ApiError> {
        StructuredLog.error(logger, "application.request.unhandled", ex)
        val body = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            errorCode = "internal.error",
            message = "Es trat ein unerwarteter Fehler auf. Bitte wenden Sie sich mit der Correlation-ID an Ihren Administrator.",
            correlationId = MDC.get(StructuredLog.MDC_CORRELATION_ID),
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
