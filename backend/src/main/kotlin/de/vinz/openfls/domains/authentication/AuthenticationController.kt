package de.vinz.openfls.domains.authentication

import de.vinz.openfls.domains.authentication.models.EUserRoles
import de.vinz.openfls.domains.authentication.dtos.PasswordDto
import de.vinz.openfls.domains.authentication.dtos.AuthenticationRequestDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
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
        } catch (ex: BadCredentialsException) {
            logger.error(ex.message, ex)

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/password")
    fun changePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                       @Valid @RequestBody passwordDto: PasswordDto): ResponseEntity<String> {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            authenticationService.changePassword(passwordDto)

            if (logPerformance) {
                logger.info(String.format("%s changePassword took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity(HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

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

            if (logPerformance) {
                logger.info(String.format("%s changeRole took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity(HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/")
    fun authCheck(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Any {
        return ResponseEntity.ok()
    }

    @GetMapping("/user")
    fun getUser(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Any {
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
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }
}