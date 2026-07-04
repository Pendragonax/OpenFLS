package de.vinz.openfls.domains.authentication

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.authentication.DisabledException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthenticationController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var authenticationService: AuthenticationService

    @Test
    fun login_inactiveEmployee_returnsUnauthorized() {
        // Given
        given(authenticationService.login("inactive", "secret"))
            .willThrow(DisabledException("User is disabled"))

        // When
        val result = mockMvc.post("/login") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
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
}
