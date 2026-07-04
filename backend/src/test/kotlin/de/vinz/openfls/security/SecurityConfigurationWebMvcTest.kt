package de.vinz.openfls.security

import de.vinz.openfls.domains.clients.ClientController
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.permissions.AccessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.security.core.userdetails.UserDetailsService

@WebMvcTest(
    controllers = [ClientController::class],
    properties = [
        "logging.performance=false",
        "jwt.private-key=classpath:private.key",
        "jwt.public-key=classpath:public.key"
    ]
)
@AutoConfigureMockMvc
@Import(SecurityConfiguration::class)
class SecurityConfigurationWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var userDetailsService: UserDetailsService

    @Test
    fun getChangelog_withoutAuthentication_returnsOk() {
        // When
        val result = mockMvc.get("/changelog/latest.md").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).isNotBlank
    }
}
