package de.vinz.openfls.domains.authentication

import de.vinz.openfls.domains.authentication.models.EUserRoles
import de.vinz.openfls.domains.authentication.dtos.PasswordDto
import de.vinz.openfls.domains.authentication.dtos.AuthenticationRequestDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.logging.StructuredLog
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.*

@RestController
class AuthenticationController(
        private val authenticationService: AuthenticationService
) {
    private val logger: Logger = LoggerFactory.getLogger(AuthenticationController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping("/login")
    fun login(@RequestBody request: AuthenticationRequestDto): ResponseEntity<Map<String, String>> {
        try {
            // performance
            val startMs = System.currentTimeMillis()

            val authentication = authenticationService.login(request.username, request.password)
            StructuredLog.audit("authentication.login", "success", "user", authentication.userId.toString())

            if (logPerformance) {
                logger.info(String.format("%s login took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, authentication.token)
                    .body(mapOf(
                            "id" to authentication.userId.toString(),
                            "token" to authentication.token,
                            "expiredAt" to authentication.expiredAt))
        } catch (ex: AuthenticationException) {
            StructuredLog.audit("authentication.login", "failure")
            StructuredLog.error(logger, "authentication.login.failed", ex)

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/password")
    fun changePassword(@Valid @RequestBody passwordDto: PasswordDto): ResponseEntity<String> {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            authenticationService.changePassword(passwordDto)
            StructuredLog.audit("authentication.password.change", "success")

            if (logPerformance) {
                logger.info(String.format("%s changePassword took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity(HttpStatus.OK)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "authentication.password.change.failed", ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/change_role/{id}")
    fun changeRole(@PathVariable id: Long,
                   @RequestBody role: Int): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            authenticationService.changeRole(id, EUserRoles.fromId(role))
            StructuredLog.audit("authorization.role.change", "success", "user", id.toString())

            if (logPerformance) {
                logger.info(String.format("%s changeRole took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity(HttpStatus.OK)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "authorization.role.change.failed", ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/")
    fun authCheck(): Any {
        return ResponseEntity.ok()
    }

    @GetMapping("/user")
    fun getUser(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val employee = authenticationService.getCurrentEmployeeDto()

            if (logPerformance) {
                logger.info(String.format("%s getUser took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            employee.orElseThrow { IllegalArgumentException() }

            return ResponseEntity.ok(employee.get())
        } catch (ex: Exception) {
            StructuredLog.error(logger, "authentication.user.read.failed", ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }
}
