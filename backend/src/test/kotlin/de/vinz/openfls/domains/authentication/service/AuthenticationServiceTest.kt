package de.vinz.openfls.domains.authentication.service

import de.vinz.openfls.domains.authentication.dto.ChangePasswordRequest
import de.vinz.openfls.domains.authentication.dto.ChangePasswordResult
import de.vinz.openfls.domains.employees.service.EmployeeAccessService
import de.vinz.openfls.domains.employees.service.EmployeeService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import java.time.Duration

class AuthenticationServiceTest {

    private val employeeAccessService: EmployeeAccessService = mock()
    private val employeeService: EmployeeService = mock()
    private val authenticationManager: AuthenticationManager = mock()
    private val jwtEncoder: JwtEncoder = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val authenticationService = AuthenticationService(
        employeeAccessService,
        employeeService,
        authenticationManager,
        jwtEncoder,
        passwordEncoder,
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
        whenever(employeeAccessService.getPasswordHashById(7L)).thenReturn(null)

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
        whenever(employeeAccessService.getPasswordHashById(7L)).thenReturn("encoded-old")
        whenever(passwordEncoder.matches("wrongpw", "encoded-old")).thenReturn(false)

        // When
        val result = authenticationService.changePassword(
            ChangePasswordRequest(oldPassword = "wrongpw", newPassword = "newpassword")
        )

        // Then
        assertThat(result).isEqualTo(ChangePasswordResult.WrongOldPassword)
        verify(employeeAccessService, org.mockito.kotlin.never()).changePassword(any(), any())
    }

    @Test
    fun changePassword_correctOldPassword_updatesAndReturnsSuccess() {
        // Given
        whenever(employeeAccessService.getPasswordHashById(7L)).thenReturn("encoded-old")
        whenever(passwordEncoder.matches("oldpassword", "encoded-old")).thenReturn(true)
        whenever(passwordEncoder.encode("newpassword")).thenReturn("encoded-new")

        // When
        val result = authenticationService.changePassword(
            ChangePasswordRequest(oldPassword = "oldpassword", newPassword = "newpassword")
        )

        // Then
        assertThat(result).isEqualTo(ChangePasswordResult.Success)
        verify(employeeAccessService).changePassword(7L, "encoded-new")
    }

    @Test
    fun getCurrentEmployee_unknownEmployee_returnsNull() {
        // Given
        whenever(employeeService.getEmployeeDetailById(7L, true)).thenReturn(null)

        // When
        val result = authenticationService.getCurrentEmployee()

        // Then
        assertThat(result).isNull()
    }
}
