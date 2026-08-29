package de.vinz.openfls.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/** Adds a correlation id to every request and records security-relevant HTTP outcomes without logging payloads. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.requestURI.startsWith("/ws")

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val correlationId = request.getHeader("X-Correlation-ID")
            ?.takeIf { it.matches(Regex("[a-zA-Z0-9._-]{8,80}")) }
            ?: UUID.randomUUID().toString()
        val startedAt = System.nanoTime()
        var requestFailed = false
        MDC.put("correlation_id", correlationId)
        response.setHeader("X-Correlation-ID", correlationId)
        try {
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            requestFailed = true
            StructuredLog.error(org.slf4j.LoggerFactory.getLogger(RequestLoggingFilter::class.java), "http.request.failed", ex)
            throw ex
        } finally {
            val method = request.method
            val path = request.requestURI
            val status = if (requestFailed) 500 else response.status
            if (method !in SAFE_METHODS || status >= 400 || path.startsWith("/admin/logs")) {
                StructuredLog.request(method, path, status, (System.nanoTime() - startedAt) / 1_000_000)
                if (method !in SAFE_METHODS && status < 400) {
                    StructuredLog.audit("http.state_change", "success", "http.path", path)
                }
            }
            MDC.remove("correlation_id")
        }
    }

    private companion object {
        val SAFE_METHODS = setOf("GET", "HEAD", "OPTIONS")
    }
}
