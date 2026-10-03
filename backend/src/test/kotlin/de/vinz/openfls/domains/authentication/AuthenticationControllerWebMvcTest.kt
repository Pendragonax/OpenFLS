package de.vinz.openfls.domains.authentication

import de.vinz.openfls.domains.authentication.dto.ChangePasswordResult
import de.vinz.openfls.domains.authentication.dto.LoginResponse
import de.vinz.openfls.domains.authentication.service.AuthenticationService
import de.vinz.openfls.domains.employees.dto.EmployeeDetailResponse
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.DisabledException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthenticationController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var authenticationService: AuthenticationService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun login_inactiveEmployee_returnsUnauthorized() {
        // Given
        given(authenticationService.login("inactive", "secret"))
            .willThrow(DisabledException("User is disabled"))

        // When
        val result = mockMvc.post("/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "username": "inactive",
                  "password": "secret"
                }
            """.trimIndent()
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(401)
    }

    @Test
    fun login_validCredentials_returnsTokenAndHeader() {
        // Given
        given(authenticationService.login("max", "secret")).willReturn(
            LoginResponse(userId = 7, token = "jwt-token", expiredAt = "2026-01-01T00:00:00Z")
        )

        // When
        val result = mockMvc.post("/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"username":"max","password":"secret"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.getHeader("Authorization")).isEqualTo("jwt-token")
        assertThat(result.response.contentAsString).contains("\"token\":\"jwt-token\"")
    }

    @Test
    fun changePassword_wrongOldPassword_returnsBadRequest() {
        // Given
        given(authenticationService.changePassword(any())).willReturn(ChangePasswordResult.WrongOldPassword)

        // When
        val result = mockMvc.post("/password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"oldPassword":"wrongpw","newPassword":"newpassword"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("old password is wrong")
    }

    @Test
    fun changePassword_employeeNotFound_returnsNotFound() {
        // Given
        given(authenticationService.changePassword(any())).willReturn(ChangePasswordResult.EmployeeNotFound)

        // When
        val result = mockMvc.post("/password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"oldPassword":"oldpassword","newPassword":"newpassword"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun changePassword_success_returnsOk() {
        // Given
        given(authenticationService.changePassword(any())).willReturn(ChangePasswordResult.Success)

        // When
        val result = mockMvc.post("/password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"oldPassword":"oldpassword","newPassword":"newpassword"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun changePassword_blankFields_returnsBadRequestWithoutCallingService() {
        // When
        val result = mockMvc.post("/password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"oldPassword":"","newPassword":""}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun getUser_noCurrentEmployee_returnsNotFound() {
        // Given
        given(authenticationService.getCurrentEmployee()).willReturn(null)

        // When
        val result = mockMvc.get("/user").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun getUser_currentEmployee_returnsEmployee() {
        // Given
        given(authenticationService.getCurrentEmployee()).willReturn(EmployeeDetailResponse(id = 7))

        // When
        val result = mockMvc.get("/user").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
    }
}
