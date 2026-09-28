package de.vinz.openfls.domains.authentication.service

import de.vinz.openfls.domains.authentication.UserRole
import de.vinz.openfls.domains.authentication.dto.ChangePasswordRequest
import de.vinz.openfls.domains.authentication.dto.ChangePasswordResult
import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.modelmapper.ModelMapper
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import java.time.Duration

class AuthenticationServiceTest {

    private val employeeAccessRepository: EmployeeAccessRepository = mock()
    private val employeeRepository: EmployeeRepository = mock()
    private val authenticationManager: AuthenticationManager = mock()
    private val jwtEncoder: JwtEncoder = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val modelMapper: ModelMapper = mock()
    private val authenticationService = AuthenticationService(
        employeeAccessRepository,
        employeeRepository,
        authenticationManager,
        jwtEncoder,
        passwordEncoder,
        modelMapper,
        Duration.ofMinutes(10)
    )

    @BeforeEach
    fun setUp() {
        authenticateAs(7L)
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    private fun authenticateAs(userId: Long) {
        val jwt: Jwt = mock()
        whenever(jwt.getClaimAsString("id")).thenReturn(userId.toString())
        val authentication: Authentication = mock()
        whenever(authentication.principal).thenReturn(jwt)
        SecurityContextHolder.setContext(SecurityContextImpl(authentication))
    }

    @Test
    fun changePassword_missingEmployee_returnsEmployeeNotFound() {
        // Given
        whenever(employeeAccessRepository.findById(7L)).thenReturn(java.util.Optional.empty())

        // When
        val result = authenticationService.changePassword(
            ChangePasswordRequest(oldPassword = "oldpassword", newPassword = "newpassword")
        )

        // Then
        assertThat(result).isEqualTo(ChangePasswordResult.EmployeeNotFound)
    }

    @Test
    fun changePassword_wrongOldPassword_returnsWrongOldPassword() {
        // Given
        val access = EmployeeAccess(id = 7, username = "max", password = "encoded-old", role = 3)
        whenever(employeeAccessRepository.findById(7L)).thenReturn(java.util.Optional.of(access))
        whenever(passwordEncoder.matches("wrongpw", "encoded-old")).thenReturn(false)

        // When
        val result = authenticationService.changePassword(
            ChangePasswordRequest(oldPassword = "wrongpw", newPassword = "newpassword")
        )

        // Then
        assertThat(result).isEqualTo(ChangePasswordResult.WrongOldPassword)
        verify(employeeAccessRepository, org.mockito.kotlin.never()).changePassword(any(), any())
    }

    @Test
    fun changePassword_correctOldPassword_updatesAndReturnsSuccess() {
        // Given
        val access = EmployeeAccess(id = 7, username = "max", password = "encoded-old", role = 3)
        whenever(employeeAccessRepository.findById(7L)).thenReturn(java.util.Optional.of(access))
        whenever(passwordEncoder.matches("oldpassword", "encoded-old")).thenReturn(true)
        whenever(passwordEncoder.encode("newpassword")).thenReturn("encoded-new")

        // When
        val result = authenticationService.changePassword(
            ChangePasswordRequest(oldPassword = "oldpassword", newPassword = "newpassword")
        )

        // Then
        assertThat(result).isEqualTo(ChangePasswordResult.Success)
        verify(employeeAccessRepository).changePassword(7L, "encoded-new")
    }

    @Test
    fun changeRole_missingEmployee_returnsFalse() {
        // Given
        whenever(employeeAccessRepository.existsById(9L)).thenReturn(false)

        // When
        val result = authenticationService.changeRole(9L, UserRole.ADMIN)

        // Then
        assertThat(result).isFalse()
        verify(employeeAccessRepository, org.mockito.kotlin.never()).changeRole(any(), any())
    }

    @Test
    fun changeRole_existingEmployee_updatesAndReturnsTrue() {
        // Given
        whenever(employeeAccessRepository.existsById(9L)).thenReturn(true)

        // When
        val result = authenticationService.changeRole(9L, UserRole.LEAD)

        // Then
        assertThat(result).isTrue()
        verify(employeeAccessRepository).changeRole(9L, UserRole.LEAD.id)
    }

    @Test
    fun getCurrentEmployee_unknownEmployee_returnsNull() {
        // Given
        whenever(employeeRepository.findById(7L)).thenReturn(java.util.Optional.empty())

        // When
        val result = authenticationService.getCurrentEmployee()

        // Then
        assertThat(result).isNull()
    }
}
