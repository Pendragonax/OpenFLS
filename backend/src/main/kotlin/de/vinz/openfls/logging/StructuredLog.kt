package de.vinz.openfls.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Central logging facade. Its methods deliberately accept only identifiers and
 * fixed event names; request payloads and credentials must never be passed to it.
 *
 * Exception details are handled by [error]: it always attaches the [Throwable] so
 * Logback renders a stacktrace, records the exception type and its root cause, and
 * adds a sanitized exception message for triage.
 */
object StructuredLog {
    private val auditLogger = LoggerFactory.getLogger("de.vinz.openfls.audit")

    /** MDC keys populated per request by `RequestLoggingFilter`. */
    const val MDC_CORRELATION_ID = "correlation_id"
    const val MDC_HTTP_METHOD = "http.method"
    const val MDC_HTTP_PATH = "http.path"

    private const val MAX_CAUSE_DEPTH = 20

    /**
     * Exception types that represent an expected, already-handled outcome (bad input,
     * missing permission, violated business rule). They are logged at `WARN`; every
     * other exception is treated as an unexpected fault and logged at `ERROR`. Both
     * cases carry the full stacktrace.
     */
    private val EXPECTED_EXCEPTION_TYPES = setOf(
        "java.lang.IllegalArgumentException",
        "java.util.NoSuchElementException",
        "org.springframework.security.access.AccessDeniedException",
        "org.springframework.web.server.ResponseStatusException",
    )

    fun audit(eventName: String, outcome: String, objectType: String? = null, objectId: String? = null) {
        auditLogger.info(fields(eventName, outcome, objectType, objectId))
    }

    fun request(method: String, path: String, status: Int, durationMs: Long) {
        val outcome = when {
            status >= 500 -> "failure"
            status >= 400 -> "denied"
            else -> "success"
        }
        val event = "http.request.completed"
        val payload = "${fields(event, outcome, "http.path", path)} http.method=${safe(method)} http.status=$status duration_ms=$durationMs"
        when {
            status >= 500 -> LoggerFactory.getLogger("de.vinz.openfls.http").error(payload)
            status >= 400 -> LoggerFactory.getLogger("de.vinz.openfls.http").warn(payload)
            else -> LoggerFactory.getLogger("de.vinz.openfls.http").info(payload)
        }
    }

    fun validationFailure(field: String) {
        LoggerFactory.getLogger("de.vinz.openfls.validation").warn(
            "${fields("input.validation.failed", "failure")} field=${safe(field)}"
        )
    }

    /**
     * Logs a failed operation with its exception. The [exception] is passed as the
     * trailing argument so SLF4J/Logback appends the stacktrace. Known, expected
     * exception types are logged at `WARN`, everything else at `ERROR`.
     */
    fun error(logger: Logger, eventName: String, exception: Throwable) {
        val rootCause = rootCause(exception)
        val base = fields(eventName, "failure")
        val exType = safe(exception.javaClass.name)
        val rootType = safe(rootCause.javaClass.name)
        val exMessage = safe(exception.message ?: "none")
        val format = "{} exception.type={} exception.root_type={} exception.message={}"
        if (isExpected(exception) || isExpected(rootCause)) {
            logger.warn(format, base, exType, rootType, exMessage, exception)
        } else {
            logger.error(format, base, exType, rootType, exMessage, exception)
        }
    }

    private fun isExpected(throwable: Throwable): Boolean {
        val name = throwable.javaClass.name
        return name in EXPECTED_EXCEPTION_TYPES ||
            (name.startsWith("de.vinz.openfls.") && name.endsWith("Exception"))
    }

    private fun rootCause(throwable: Throwable): Throwable {
        var current = throwable
        var depth = 0
        while (current.cause != null && current.cause !== current && depth < MAX_CAUSE_DEPTH) {
            current = current.cause!!
            depth++
        }
        return current
    }

    private fun fields(eventName: String, outcome: String, objectType: String? = null, objectId: String? = null): String {
        val actor = currentActorId()
        return buildList {
            add("event_name=${safe(eventName)}")
            add("outcome=${safe(outcome)}")
            MDC.get(MDC_CORRELATION_ID)?.let { add("correlation_id=${safe(it)}") }
            MDC.get(MDC_HTTP_METHOD)?.let { add("http.method=${safe(it)}") }
            MDC.get(MDC_HTTP_PATH)?.let { add("http.path=${safe(it)}") }
            actor?.let { add("user_id=$it") }
            objectType?.let { add("object_type=${safe(it)}") }
            objectId?.let { add("object_id=${safe(it)}") }
        }.joinToString(" ")
    }

    private fun currentActorId(): String? = try {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        (principal as? Jwt)?.getClaimAsString("id")?.takeIf { it.matches(Regex("\\d+")) }
    } catch (_: RuntimeException) {
        null
    }

    private fun safe(value: String): String = value
        .replace(Regex("[\\r\\n\\t]"), "_")
        .replace(Regex("[^a-zA-Z0-9._:/=-]"), "_")
        .take(160)
}
