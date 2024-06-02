package de.vinz.openfls.controller

import de.vinz.openfls.dtos.*
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.entities.*
import de.vinz.openfls.services.EmployeeService
import de.vinz.openfls.services.TokenService
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.bind.annotation.*
import java.lang.String.format
import java.time.Instant
import java.util.stream.Collectors.joining
import jakarta.validation.Valid

@RestController
class AuthController(
    private val employeeService: EmployeeService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtEncoder: JwtEncoder,
    private val tokenService: TokenService,
    private val modelMapper: ModelMapper
) {
    private val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<Map<String, String>> {
        try {
            // performance
            val startMs = System.currentTimeMillis()

            val authentication = authenticationManager
                .authenticate(UsernamePasswordAuthenticationToken(request.username, request.password))

            val user = authentication?.principal as CustomUserDetails

            val now = Instant.now()
            val expiry = System.getenv("SESSION_TIMEOUT").toLong()

            val scope = authentication.authorities?.stream()
                ?.map(GrantedAuthority::getAuthority)
                ?.collect(joining(" "))

            val claims = JwtClaimsSet.builder()
                .issuer("openfls")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(format("%s", user.username))
                .claim("roles", scope)
                .claim("id", user.getId())
                .build()

            val token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims))?.tokenValue

            if (logPerformance) {
                logger.info(String.format("%s login took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(mapOf(
                    "id" to user.getId().toString(),
                    "token" to (token?: ""),
                    "expiredAt" to now.plusSeconds(expiry).toString()))
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

            val id = tokenService.getUserInfo(token).first
            passwordDto.newPassword = passwordEncoder.encode(passwordDto.newPassword) ?: passwordDto.newPassword

            employeeService.changePassword(id, passwordDto)

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

            employeeService.changeRole(id, role)

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

            val id = tokenService.getUserInfo(token).first

            // default admin logged in
            if (id == 0L) {
                return ResponseEntity.ok(EmployeeDto().apply {
                    this.id = 0
                    firstName = "ADMIN"
                    lastName = "ADMIN"
                    access = EmployeeAccessDto().apply {
                        this.id = 0
                        this.username = "admin"
                        this.password = ""
                        this.role = 1
                    }
                    permissions = emptyArray()
                })
            }

            // load user
            val employee = employeeService.getById(id, true) ?: throw IllegalArgumentException()

            val employeeDto = modelMapper.map(employee, EmployeeDto::class.java).apply {
                access = employee.access?.let {
                    modelMapper.map(it, EmployeeAccessDto::class.java).apply {
                        password = ""
                    }
                }
                permissions = employee.permissions
                    ?.map { modelMapper.map(it, PermissionDto::class.java) }
                    ?.toTypedArray()
            }

            if (logPerformance) {
                logger.info(String.format("%s getUser took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            return ResponseEntity.ok(employeeDto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }
}