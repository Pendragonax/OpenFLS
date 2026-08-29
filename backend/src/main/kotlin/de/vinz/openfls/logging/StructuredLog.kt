package de.vinz.openfls.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Central logging facade. Its methods deliberately accept only identifiers and
 * fixed event names; request payloads, credentials and exception messages must
 * never be passed to it.
 */
object StructuredLog {
    private val auditLogger = LoggerFactory.getLogger("de.vinz.openfls.audit")

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

    fun error(logger: Logger, eventName: String, exception: Throwable) {
        logger.error(
            "${fields(eventName, "failure")} exception.type=${safe(exception.javaClass.name)} message=Unhandled_application_error"
        )
    }

    private fun fields(eventName: String, outcome: String, objectType: String? = null, objectId: String? = null): String {
        val actor = currentActorId()
        return buildList {
            add("event_name=${safe(eventName)}")
            add("outcome=${safe(outcome)}")
            MDC.get("correlation_id")?.let { add("correlation_id=${safe(it)}") }
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
