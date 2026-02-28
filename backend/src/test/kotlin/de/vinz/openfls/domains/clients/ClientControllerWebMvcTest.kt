package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dtos.ClientForServiceEditingDto
import de.vinz.openfls.domains.permissions.AccessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ClientController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun getForServiceEditingById_validRequest_returnsClientDto() {
        // Given
        val userId = 8L
        val clientId = 3L
        val allowedInstitutions = listOf(11L, 12L)
        val dto = ClientForServiceEditingDto().apply {
            id = clientId
            firstName = "Max"
            lastName = "Mustermann"
        }
        given(accessService.getId()).willReturn(userId)
        given(accessService.getWriteRightsInstitutionIds(userId)).willReturn(allowedInstitutions)
        given(clientService.getForServiceEditingById(clientId, allowedInstitutions)).willReturn(dto)

        // When
        val result = mockMvc.get("/clients/for-service-editing/$clientId").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
        assertThat(result.response.contentAsString).contains("\"firstName\":\"Max\"")
        assertThat(result.response.contentAsString).contains("\"lastName\":\"Mustermann\"")
    }

    @Test
    fun getForServiceEditingById_accessServiceThrows_returnsBadRequest() {
        // Given
        doThrow(IllegalArgumentException("boom"))
            .`when`(accessService)
            .getId()

        // When
        val result = mockMvc.get("/clients/for-service-editing/3").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }
}
