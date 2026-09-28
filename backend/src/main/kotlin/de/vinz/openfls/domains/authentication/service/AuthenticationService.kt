package de.vinz.openfls.domains.authentication.service

import de.vinz.openfls.domains.authentication.UserRole
import de.vinz.openfls.domains.authentication.dto.ChangePasswordRequest
import de.vinz.openfls.domains.authentication.dto.ChangePasswordResult
import de.vinz.openfls.domains.authentication.dto.LoginResponse
import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.dtos.EmployeeAccessDto
import de.vinz.openfls.domains.employees.dtos.EmployeeWithAccess
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.permissions.dto.PermissionResponse
import de.vinz.openfls.security.CustomUserDetails
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
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
        private val employeeAccessRepository: EmployeeAccessRepository,
        private val employeeRepository: EmployeeRepository,
        private val authenticationManager: AuthenticationManager,
        private val jwtEncoder: JwtEncoder,
        private val passwordEncoder: PasswordEncoder,
        private val modelMapper: ModelMapper,
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
        val access = employeeAccessRepository.findByIdOrNull(userId)
            ?: return ChangePasswordResult.EmployeeNotFound

        if (!passwordEncoder.matches(request.oldPassword, access.password)) {
            return ChangePasswordResult.WrongOldPassword
        }

        val newEncryptedPassword = passwordEncoder.encode(request.newPassword).orEmpty()
        employeeAccessRepository.changePassword(userId, newEncryptedPassword)
        return ChangePasswordResult.Success
    }

    @Transactional
    fun changeRole(userId: Long, role: UserRole): Boolean {
        if (!employeeAccessRepository.existsById(userId)) {
            return false
        }

        employeeAccessRepository.changeRole(userId, role.id)
        return true
    }

    @Transactional(readOnly = true)
    fun getCurrentEmployee(): EmployeeWithAccess? {
        val employee = getCurrentEmployeeEntity() ?: return null

        return modelMapper.map(employee, EmployeeWithAccess::class.java).apply {
            access = employee.access?.let {
                modelMapper.map(it, EmployeeAccessDto::class.java).apply {
                    password = ""
                }
            }
            permissions = employee.permissions
                    ?.map { PermissionResponse.from(it) }
                    ?.toList()
        }
    }

    private fun getCurrentEmployeeEntity(): Employee? {
        val userId = getCurrentUserId()

        // initial admin from CustomUserDetailsService
        if (userId == 0L) {
            return getInitialAdminEmployee()
        }

        return employeeRepository.findByIdOrNull(userId)
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
