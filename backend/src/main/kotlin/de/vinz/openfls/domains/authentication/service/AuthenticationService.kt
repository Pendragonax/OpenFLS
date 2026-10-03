package de.vinz.openfls.domains.authentication.service

import de.vinz.openfls.domains.authentication.UserRole
import de.vinz.openfls.domains.authentication.dto.ChangePasswordRequest
import de.vinz.openfls.domains.authentication.dto.ChangePasswordResult
import de.vinz.openfls.domains.authentication.dto.LoginResponse
import de.vinz.openfls.domains.employees.dto.EmployeeDetailResponse
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import de.vinz.openfls.domains.employees.service.EmployeeAccessService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.security.CustomUserDetails
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.stream.Collectors

@Service
class AuthenticationService(
        private val employeeAccessService: EmployeeAccessService,
        private val employeeService: EmployeeService,
        private val authenticationManager: AuthenticationManager,
        private val jwtEncoder: JwtEncoder,
        private val passwordEncoder: PasswordEncoder,
        @param:Value("\${server.servlet.session.timeout}") private val sessionTimeout: Duration
) {
    @Transactional(readOnly = true)
    fun login(username: String, password: String): LoginResponse {
        val authentication = authenticationManager
                .authenticate(UsernamePasswordAuthenticationToken(username, password))

        val user = authentication.principal as CustomUserDetails

        val now = Instant.now()
        val expireAfterSeconds = sessionTimeout.seconds

        val scope = authentication.authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "))

        val claims = JwtClaimsSet.builder()
                .issuer("openfls")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expireAfterSeconds))
                .subject(java.lang.String.format("%s", user.username))
                .claim("roles", scope)
                .claim("id", user.getId())
                .build()

        val token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims))?.tokenValue

        return LoginResponse(
                user.getId(),
                token ?: "",
                now.plusSeconds(expireAfterSeconds).toString()
        )
    }

    @Transactional
    fun changePassword(request: ChangePasswordRequest): ChangePasswordResult {
        val userId = getCurrentUserId()
        val encodedPassword = employeeAccessService.getPasswordHashById(userId)
            ?: return ChangePasswordResult.EmployeeNotFound

        if (!passwordEncoder.matches(request.oldPassword, encodedPassword)) {
            return ChangePasswordResult.WrongOldPassword
        }

        val newEncryptedPassword = passwordEncoder.encode(request.newPassword).orEmpty()
        employeeAccessService.changePassword(userId, newEncryptedPassword)
        return ChangePasswordResult.Success
    }

    @Transactional(readOnly = true)
    fun getCurrentEmployee(): EmployeeDetailResponse? {
        val userId = getCurrentUserId()

        // initial admin from CustomUserDetailsService
        if (userId == 0L) {
            return EmployeeDetailResponse.from(getInitialAdminEmployee())
        }

        return employeeService.getEmployeeDetailById(userId, includeArchived = true)
    }

    private fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication present")
        val jwt = authentication.principal as Jwt
        return jwt.getClaimAsString("id").toLong()
    }

    private fun getInitialAdminEmployee(): Employee {
        return Employee(
                id = 0,
                firstname = "Initial",
                lastname = "Administrator",
                phonenumber = "",
                email = "",
                inactive = false,
                description = "",
                access = EmployeeAccess(
                        id = 0,
                        username = "admin",
                        password = passwordEncoder.encode("admin").orEmpty(),
                        role = UserRole.ADMIN.id,
                        employee = null
                ),
                permissions = mutableSetOf(),
                unprofessionals = mutableSetOf(),
                contingents = mutableSetOf(),
                createdEvaluations = mutableSetOf(),
                updatedEvaluations = mutableSetOf(),
                services = mutableSetOf(),
                assistancePlanFavorites = mutableSetOf()
        )
    }
}
