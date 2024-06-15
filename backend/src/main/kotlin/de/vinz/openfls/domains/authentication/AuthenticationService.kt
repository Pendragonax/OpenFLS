package de.vinz.openfls.domains.authentication

import de.vinz.openfls.domains.authentication.dtos.AuthenticationResponseDto
import de.vinz.openfls.domains.authentication.models.EUserRoles
import de.vinz.openfls.domains.employees.dtos.EmployeeAccessDto
import de.vinz.openfls.domains.employees.dtos.EmployeeDto
import de.vinz.openfls.domains.authentication.dtos.PasswordDto
import de.vinz.openfls.dtos.PermissionDto
import de.vinz.openfls.security.CustomUserDetails
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper
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
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

@Service
class AuthenticationService(
        private val employeeAccessRepository: EmployeeAccessRepository,
        private val employeeRepository: EmployeeRepository,
        private val authenticationManager: AuthenticationManager,
        private val jwtEncoder: JwtEncoder,
        private val passwordEncoder: PasswordEncoder,
        private val modelMapper: ModelMapper
) {
    fun login(username: String, password: String): AuthenticationResponseDto {
        val authentication = authenticationManager
                .authenticate(UsernamePasswordAuthenticationToken(username, password))

        val user = authentication?.principal as CustomUserDetails

        val now = Instant.now()
        val expireAfterSeconds = System.getenv("SESSION_TIMEOUT").toLong()

        val scope = authentication.authorities?.stream()
                ?.map(GrantedAuthority::getAuthority)
                ?.collect(Collectors.joining(" "))

        val claims = JwtClaimsSet.builder()
                .issuer("openfls")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expireAfterSeconds))
                .subject(java.lang.String.format("%s", user.username))
                .claim("roles", scope)
                .claim("id", user.getId())
                .build()

        val token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims))?.tokenValue

        return AuthenticationResponseDto(
                user.getId(),
                token ?: "",
                now.plusSeconds(expireAfterSeconds).toString()
        )
    }

    @Transactional
    fun changePassword(passwordDto: PasswordDto) {
        val userId = getCurrentUserId()

        val newEncryptedPassword = passwordEncoder.encode(passwordDto.newPassword) ?: passwordDto.newPassword

        if (passwordDto.oldPassword.isEmpty())
            throw IllegalArgumentException("old password is empty")
        if (passwordDto.newPassword.isEmpty())
            throw IllegalArgumentException("new password is empty")

        employeeAccessRepository.findById(userId).orElse(null)?.also {
            if (!passwordEncoder.matches(passwordDto.oldPassword, it.password))
                throw IllegalArgumentException("old password is wrong")

            employeeAccessRepository.changePassword(userId, newEncryptedPassword)
        } ?: throw IllegalArgumentException("employee doesnt exists")
    }

    @Transactional
    fun changeRole(userId: Long, role: EUserRoles) {
        employeeAccessRepository.findById(userId).orElse(null)?.also {
            employeeAccessRepository.changeRole(userId, role.id)
        } ?: throw IllegalArgumentException("employee doesnt exists")
    }

    fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        val jwt: Jwt = authentication.principal as Jwt
        return jwt.getClaimAsString("id").toLong()
    }

    fun getCurrentEmployeeDto(): Optional<EmployeeDto> {
        val employeeOptional = getCurrentEmployee()

        if (employeeOptional.isPresent) {
            val employee = employeeOptional.get()
            return Optional.of(modelMapper.map(employee, EmployeeDto::class.java).apply {
                access = employee.access?.let {
                    modelMapper.map(it, EmployeeAccessDto::class.java).apply {
                        password = ""
                    }
                }
                permissions = employee.permissions
                        ?.map { modelMapper.map(it, PermissionDto::class.java) }
                        ?.toTypedArray()
            })
        }

        return Optional.empty()
    }

    fun getCurrentEmployee(): Optional<Employee> {
        val userId = getCurrentUserId()

        // initial admin from CustomUserDetailsService
        if (userId == 0L) {
            return Optional.of(getInitialAdminEmployee())
        }

        return employeeRepository.findById(getCurrentUserId())
    }

    fun getInitialAdminEmployee(): Employee {
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
                        password = passwordEncoder.encode("admin"),
                        role = EUserRoles.ADMIN.id,
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