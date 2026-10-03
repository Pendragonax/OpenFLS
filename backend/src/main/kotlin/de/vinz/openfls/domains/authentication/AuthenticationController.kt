package de.vinz.openfls.domains.authentication

import de.vinz.openfls.domains.authentication.dto.ChangePasswordRequest
import de.vinz.openfls.domains.authentication.dto.ChangePasswordResult
import de.vinz.openfls.domains.authentication.dto.LoginRequest
import de.vinz.openfls.domains.authentication.service.AuthenticationService
import de.vinz.openfls.logging.StructuredLog
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.*

@RestController
class AuthenticationController(
        private val authenticationService: AuthenticationService,
        private val performanceLoggingService: PerformanceLoggingService
) {
    private val logger: Logger = LoggerFactory.getLogger(AuthenticationController::class.java)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Map<String, String>> {
        val startMs = System.currentTimeMillis()

        try {
            val authentication = authenticationService.login(request.username, request.password)
            StructuredLog.audit("authentication.login", "success", "user", authentication.userId.toString())

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
        } finally {
            performanceLoggingService.logPerformance("login", startMs, logger)
        }
    }

    @PostMapping("/password")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (authenticationService.changePassword(request)) {
                ChangePasswordResult.Success -> {
                    StructuredLog.audit("authentication.password.change", "success")
                    ResponseEntity(HttpStatus.OK)
                }
                ChangePasswordResult.EmployeeNotFound -> employeeNotFound()
                ChangePasswordResult.WrongOldPassword -> wrongOldPassword()
            }
        } catch (ex: Exception) {
            StructuredLog.error(logger, "authentication.password.change.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("changePassword", startMs, logger)
        }
    }

    @GetMapping("/")
    fun authCheck(): Any {
        return ResponseEntity.ok()
    }

    @GetMapping("/user")
    fun getUser(): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val employee = authenticationService.getCurrentEmployee() ?: return userNotFound()
            ResponseEntity.ok(employee)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "authentication.user.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getUser", startMs, logger)
        }
    }

    private fun employeeNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("employee not found")

    private fun wrongOldPassword(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body("old password is wrong")

    private fun userNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found")
}
