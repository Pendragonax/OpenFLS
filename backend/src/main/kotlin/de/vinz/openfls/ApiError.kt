package de.vinz.openfls

import java.time.Instant

/**
 * Stable error body for otherwise unhandled failures.
 *
 * [correlationId] matches the `X-Correlation-ID` response header and the
 * corresponding server log entry, so a user-reported failure can be traced to a
 * single log line. [errorCode] is a stable, machine-readable code the frontend can
 * branch on; [message] is a generic, non-sensitive text for display.
 */
data class ApiError(
    val timestamp: String = Instant.now().toString(),
    val status: Int,
    val errorCode: String,
    val message: String,
    val correlationId: String?,
)
