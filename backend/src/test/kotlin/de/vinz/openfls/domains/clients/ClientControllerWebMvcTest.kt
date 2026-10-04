package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dto.ClientCreateResult
import de.vinz.openfls.domains.clients.dto.ClientDeleteResult
import de.vinz.openfls.domains.clients.dto.ClientDetailResponse
import de.vinz.openfls.domains.clients.dto.ClientForServiceEditingResponse
import de.vinz.openfls.domains.clients.dto.ClientUpdateResult
import de.vinz.openfls.domains.clients.service.ClientDeletionService
import de.vinz.openfls.domains.clients.service.ClientForServiceEditingService
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.categories.dto.CategoryTemplateWithCategoriesResponse
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(ClientController::class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientService: ClientService

    @MockitoBean
    lateinit var clientDeletionService: ClientDeletionService

    @MockitoBean
    lateinit var clientForServiceEditingService: ClientForServiceEditingService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun create_leaderOfInstitution_returnsClient() {
        given(accessService.isLeader(5L)).willReturn(true)
        given(clientService.create(any())).willReturn(ClientCreateResult.Success(detail(9L)))

        val result = postCreate()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":9")
    }

    @Test
    fun create_withoutLeaderRights_returnsForbidden() {
        given(accessService.isLeader(5L)).willReturn(false)

        val result = postCreate()

        assertThat(result.response.status).isEqualTo(403)
        verify(clientService, never()).create(any())
    }

    @Test
    fun create_unknownInstitution_returnsBadRequest() {
        given(accessService.isLeader(5L)).willReturn(true)
        given(clientService.create(any())).willReturn(ClientCreateResult.InstitutionNotFound)

        assertThat(postCreate().response.status).isEqualTo(400)
    }

    @Test
    fun update_differentPathAndBodyId_returnsBadRequest() {
        assertThat(putUpdate(pathId = 3L, bodyId = 4L).response.status).isEqualTo(400)
    }

    @Test
    fun update_withoutPermission_returnsForbidden() {
        given(accessService.canModifyClient(3L)).willReturn(false)

        val result = putUpdate(pathId = 3L, bodyId = 3L)

        assertThat(result.response.status).isEqualTo(403)
        verify(clientService, never()).update(any())
    }

    @Test
    fun update_archivedClient_returnsConflict() {
        given(accessService.canModifyClient(3L)).willReturn(true)
        given(clientService.update(any())).willReturn(ClientUpdateResult.Archived)

        assertThat(putUpdate(pathId = 3L, bodyId = 3L).response.status).isEqualTo(409)
    }

    @Test
    fun update_unknownClient_returnsNotFound() {
        given(accessService.canModifyClient(3L)).willReturn(true)
        given(clientService.update(any())).willReturn(ClientUpdateResult.NotFound)

        assertThat(putUpdate(pathId = 3L, bodyId = 3L).response.status).isEqualTo(404)
    }

    @Test
    fun update_validRequest_returnsClient() {
        given(accessService.canModifyClient(3L)).willReturn(true)
        given(clientService.update(any())).willReturn(ClientUpdateResult.Success(detail(3L)))

        val result = putUpdate(pathId = 3L, bodyId = 3L)

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun delete_nonAdmin_returnsForbidden() {
        given(accessService.isAdmin()).willReturn(false)

        val result = mockMvc.delete("/clients/3").andReturn()

        assertThat(result.response.status).isEqualTo(403)
        verify(clientDeletionService, never()).delete(any())
    }

    @Test
    fun delete_archivedClient_returnsConflict() {
        given(accessService.isAdmin()).willReturn(true)
        given(clientDeletionService.delete(3L)).willReturn(ClientDeleteResult.Archived)

        assertThat(mockMvc.delete("/clients/3").andReturn().response.status).isEqualTo(409)
    }

    @Test
    fun delete_unknownClient_returnsNotFound() {
        given(accessService.isAdmin()).willReturn(true)
        given(clientDeletionService.delete(3L)).willReturn(ClientDeleteResult.NotFound)

        assertThat(mockMvc.delete("/clients/3").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun delete_admin_returnsDeletedClient() {
        given(accessService.isAdmin()).willReturn(true)
        given(clientDeletionService.delete(3L)).willReturn(ClientDeleteResult.Success(detail(3L)))

        val result = mockMvc.delete("/clients/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun getById_admin_returnsClient() {
        given(accessService.isAdmin()).willReturn(true)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(clientService.getById(7L, true, emptyList())).willReturn(detail(7L, archived = true))

        val result = mockMvc.get("/clients/7").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
        assertThat(result.response.contentAsString).contains("\"archived\":true")
    }

    @Test
    fun getById_hiddenClient_returnsNotFound() {
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(clientService.getById(7L, false, emptyList())).willReturn(null)

        assertThat(mockMvc.get("/clients/7").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getForServiceEditingById_returnsClient() {
        given(clientForServiceEditingService.getForServiceEditingById(3L)).willReturn(
            ClientForServiceEditingResponse(
                id = 3L,
                firstName = "Max",
                lastName = "Mustermann",
                phoneNumber = "",
                email = "",
                archived = false,
                categoryTemplate = CategoryTemplateWithCategoriesResponse(),
                institution = InstitutionResponse(),
                assistancePlans = emptyList()
            )
        )

        val result = mockMvc.get("/clients/for-service-editing/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
        assertThat(result.response.contentAsString).contains("\"firstName\":\"Max\"")
        assertThat(result.response.contentAsString).contains("\"lastName\":\"Mustermann\"")
    }

    @Test
    fun getForServiceEditingById_unknownOrHiddenClient_returnsNotFound() {
        given(clientForServiceEditingService.getForServiceEditingById(3L)).willReturn(null)

        assertThat(mockMvc.get("/clients/for-service-editing/3").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getForServiceEditingById_serviceThrows_returnsBadRequest() {
        doThrow(IllegalArgumentException("boom")).`when`(clientForServiceEditingService).getForServiceEditingById(3L)

        assertThat(mockMvc.get("/clients/for-service-editing/3").andReturn().response.status).isEqualTo(400)
    }

    private fun postCreate() = mockMvc.post("/clients") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"firstName":"Max","lastName":"Mustermann","institutionId":5,"categoryTemplateId":2}"""
    }.andReturn()

    private fun putUpdate(pathId: Long, bodyId: Long) = mockMvc.put("/clients/$pathId") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"id":$bodyId,"firstName":"Max","lastName":"Mustermann","institutionId":5,"categoryTemplateId":2}"""
    }.andReturn()

    private fun detail(id: Long, archived: Boolean = false) = ClientDetailResponse(
        id = id,
        firstName = "Max",
        lastName = "Mustermann",
        phoneNumber = "",
        email = "",
        archived = archived,
        institution = InstitutionResponse(id = 5L, name = "Inst"),
        categoryTemplateId = 2L,
        categoryTemplateTitle = "Template"
    )
}
