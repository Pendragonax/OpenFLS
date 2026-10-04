package de.vinz.openfls.domains.services

import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.services.dto.ClientServicesByDateRequest
import de.vinz.openfls.domains.services.dto.ClientServicesByDateResponse
import de.vinz.openfls.domains.services.dto.ServiceCreateRequest
import de.vinz.openfls.domains.services.dto.ServiceCreateResult
import de.vinz.openfls.domains.services.dto.ServiceDeleteResult
import de.vinz.openfls.domains.services.dto.ServiceGetResult
import de.vinz.openfls.domains.services.dto.ServiceUpdateRequest
import de.vinz.openfls.domains.services.dto.ServiceUpdateResult
import de.vinz.openfls.domains.services.dto.ServiceWithGoalsAndCategoriesResponse
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
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
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(ServiceController::class)
@AutoConfigureMockMvc(addFilters = false)
class ServiceControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var serviceService: ServiceService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    // wie vom Frontend gesendet: vollständige Objekte für Ziele/Kategorien, dazu ignorierte Felder wie employeeId
    private val createJson = """
        {"id":0,"start":"2026-02-01T09:00:00","end":"2026-02-01T10:30:00","title":"Titel","content":"Inhalt",
         "unfinished":false,"groupService":false,"archivedService":false,"minutes":0,"employeeId":4,"clientId":1,
         "institutionId":2,"assistancePlanId":3,"hourTypeId":5,
         "goals":[{"id":6,"title":"Ziel","description":"","hours":[]}],
         "categorys":[{"id":7,"title":"Kat","shortcut":"K"}]}
    """.trimIndent()

    private val createRequest = ServiceCreateRequest(
        start = LocalDateTime.of(2026, 2, 1, 9, 0), end = LocalDateTime.of(2026, 2, 1, 10, 30),
        title = "Titel", content = "Inhalt", clientId = 1, institutionId = 2, assistancePlanId = 3, hourTypeId = 5,
        goals = listOf(de.vinz.openfls.domains.services.dto.IdReferenceRequest(6)),
        categorys = listOf(de.vinz.openfls.domains.services.dto.IdReferenceRequest(7))
    )

    private val updateJson = createJson.replace("\"id\":0", "\"id\":9")
    private val updateRequest = ServiceUpdateRequest(
        id = 9, start = createRequest.start, end = createRequest.end, title = "Titel", content = "Inhalt",
        clientId = 1, institutionId = 2, assistancePlanId = 3, hourTypeId = 5,
        goals = createRequest.goals, categorys = createRequest.categorys
    )

    private fun response(id: Long = 9) = ServiceWithGoalsAndCategoriesResponse(
        id = id, start = createRequest.start, end = createRequest.end, title = "Titel", content = "Inhalt",
        unfinished = false, groupService = false, archivedService = false, minutes = 90, employeeId = 4,
        clientId = 1, institutionId = 2, assistancePlanId = 3, hourTypeId = 5, goals = emptySet(), categorys = emptySet()
    )

    private fun postStatus(json: String = createJson): Int = mockMvc.post("/services") {
        contentType = MediaType.APPLICATION_JSON
        content = json
    }.andReturn().response.status

    private fun putStatus(path: String = "/services/9", json: String = updateJson): Int = mockMvc.put(path) {
        contentType = MediaType.APPLICATION_JSON
        content = json
    }.andReturn().response.status

    @Test
    fun create_success_returnsOk() {
        given(serviceService.create(createRequest)).willReturn(ServiceCreateResult.Success(response()))

        val result = mockMvc.post("/services") {
            contentType = MediaType.APPLICATION_JSON
            content = createJson
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"employeeId\":4")
    }

    @Test
    fun create_resultsAreMappedToStatusCodes() {
        val expected = mapOf(
            ServiceCreateResult.Forbidden to 403,
            ServiceCreateResult.ClientNotFound to 400,
            ServiceCreateResult.AssistancePlanNotFound to 400,
            ServiceCreateResult.HourTypeNotFound to 400,
            ServiceCreateResult.InstitutionNotFound to 400,
            ServiceCreateResult.GoalNotFound to 400,
            ServiceCreateResult.CategoryNotFound to 400,
            ServiceCreateResult.ClientArchived to 409,
            ServiceCreateResult.InvalidTimeRange to 400
        )
        expected.forEach { (result, status) ->
            given(serviceService.create(createRequest)).willReturn(result)
            assertThat(postStatus()).describedAs(result.toString()).isEqualTo(status)
        }
    }

    @Test
    fun update_success_returnsOk() {
        given(serviceService.update(9L, updateRequest)).willReturn(ServiceUpdateResult.Success(response()))

        assertThat(putStatus()).isEqualTo(200)
    }

    @Test
    fun update_pathIdDiffersFromBodyId_returnsBadRequestWithoutCallingTheService() {
        assertThat(putStatus(path = "/services/10")).isEqualTo(400)

        verify(serviceService, never()).update(any(), any())
    }

    @Test
    fun update_resultsAreMappedToStatusCodes() {
        val expected = mapOf(
            ServiceUpdateResult.NotFound to 404,
            ServiceUpdateResult.Forbidden to 403,
            ServiceUpdateResult.ClientNotFound to 400,
            ServiceUpdateResult.AssistancePlanNotFound to 400,
            ServiceUpdateResult.HourTypeNotFound to 400,
            ServiceUpdateResult.InstitutionNotFound to 400,
            ServiceUpdateResult.GoalNotFound to 400,
            ServiceUpdateResult.CategoryNotFound to 400,
            ServiceUpdateResult.ClientArchived to 409,
            ServiceUpdateResult.InvalidTimeRange to 400
        )
        expected.forEach { (result, status) ->
            given(serviceService.update(9L, updateRequest)).willReturn(result)
            assertThat(putStatus()).describedAs(result.toString()).isEqualTo(status)
        }
    }

    @Test
    fun delete_success_returnsOk() {
        given(serviceService.delete(9L)).willReturn(ServiceDeleteResult.Success(response()))

        assertThat(mockMvc.delete("/services/9").andReturn().response.status).isEqualTo(200)
    }

    @Test
    fun delete_resultsAreMappedToStatusCodes() {
        val expected = mapOf(
            ServiceDeleteResult.NotFound to 404,
            ServiceDeleteResult.Forbidden to 403,
            ServiceDeleteResult.ClientArchived to 409
        )
        expected.forEach { (result, status) ->
            given(serviceService.delete(9L)).willReturn(result)
            assertThat(mockMvc.delete("/services/9").andReturn().response.status)
                .describedAs(result.toString()).isEqualTo(status)
        }
    }

    @Test
    fun getById_resultsAreMappedToStatusCodes() {
        given(serviceService.getById(9L)).willReturn(ServiceGetResult.Success(response()))
        assertThat(mockMvc.get("/services/9").andReturn().response.status).isEqualTo(200)

        given(serviceService.getById(9L)).willReturn(ServiceGetResult.NotFound)
        assertThat(mockMvc.get("/services/9").andReturn().response.status).isEqualTo(404)

        given(serviceService.getById(9L)).willReturn(ServiceGetResult.Forbidden)
        assertThat(mockMvc.get("/services/9").andReturn().response.status).isEqualTo(403)
    }

    @Test
    fun getServicesByAssistancePlanId_withoutPermission_returnsForbiddenWithoutCallingTheService() {
        given(accessService.canModifyAssistancePlan(3L)).willReturn(false)

        assertThat(mockMvc.get("/services/assistance_plan/3").andReturn().response.status).isEqualTo(403)
        verify(serviceService, never()).getServicesByAssistancePlanId(any())
    }

    @Test
    fun getServicesByAssistancePlanId_unknownPlan_returnsNotFound() {
        given(accessService.canModifyAssistancePlan(3L)).willReturn(true)
        given(serviceService.getServicesByAssistancePlanId(3L)).willReturn(null)

        assertThat(mockMvc.get("/services/assistance_plan/3").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getServicesByAssistancePlanId_knownPlan_returnsServices() {
        given(accessService.canModifyAssistancePlan(3L)).willReturn(true)
        given(serviceService.getServicesByAssistancePlanId(3L)).willReturn(listOf(response()))

        val result = mockMvc.get("/services/assistance_plan/3").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"assistancePlanId\":3")
    }

    @Test
    fun getServicesOutsideAssistancePlanPeriodByEmployeeId_foreignEmployeeWithoutPermission_returnsForbidden() {
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.canReadEmployee(4L)).willReturn(false)
        given(accessService.getId()).willReturn(1L)

        assertThat(mockMvc.get("/services/employee/4/outside_assistance_plan_period").andReturn().response.status).isEqualTo(403)
        verify(serviceService, never()).getServicesOutsideAssistancePlanPeriodByEmployeeId(any())
    }

    @Test
    fun getServicesOutsideAssistancePlanPeriodByEmployeeId_ownServices_returnsList() {
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.canReadEmployee(4L)).willReturn(false)
        given(accessService.getId()).willReturn(4L)
        given(serviceService.getServicesOutsideAssistancePlanPeriodByEmployeeId(4L)).willReturn(emptyList())

        assertThat(mockMvc.get("/services/employee/4/outside_assistance_plan_period").andReturn().response.status).isEqualTo(200)
    }

    @Test
    fun getServicesOutsideAssistancePlanPeriodByInstitutionId_withoutReadAccess_returnsForbidden() {
        given(accessService.canReadEntries(2L)).willReturn(false)

        assertThat(mockMvc.get("/services/institution/2/outside_assistance_plan_period").andReturn().response.status).isEqualTo(403)
    }

    @Test
    fun getServicesByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd_foreignEmployeeWithoutPermission_returnsForbidden() {
        given(accessService.canReadEntries(2L)).willReturn(true)
        given(accessService.getId()).willReturn(1L)
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.canReadEmployee(4L)).willReturn(false)

        val result = mockMvc.get("/services/institution/2/employee/4/client/1/2026-02-01/2026-02-28").andReturn()

        assertThat(result.response.status).isEqualTo(403)
    }

    @Test
    fun getServicesByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd_allowed_returnsList() {
        given(accessService.canReadEntries(2L)).willReturn(true)
        given(accessService.getId()).willReturn(4L)
        given(serviceService.getServicesByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(
            2L, 4L, 1L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)
        )).willReturn(emptyList())

        val result = mockMvc.get("/services/institution/2/employee/4/client/1/2026-02-01/2026-02-28").andReturn()

        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun getServicesByEmployeeIdAndStartAndEnd_returnsList() {
        given(serviceService.getServicesByEmployeeIdAndStartAndEnd(4L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
            .willReturn(emptyList())

        assertThat(mockMvc.get("/services/employee/4/2026-02-01/2026-02-28").andReturn().response.status).isEqualTo(200)
    }

    @Test
    fun countServices_returnTheCounts() {
        given(serviceService.countServicesByEmployeeId(4L)).willReturn(3L)
        given(serviceService.countServicesByClientId(1L)).willReturn(5L)
        given(serviceService.countServicesByAssistancePlanId(3L)).willReturn(7L)

        assertThat(mockMvc.get("/services/count/employee/4").andReturn().response.contentAsString).isEqualTo("3")
        assertThat(mockMvc.get("/services/count/client/1").andReturn().response.contentAsString).isEqualTo("5")
        assertThat(mockMvc.get("/services/count/assistance_plan/3").andReturn().response.contentAsString).isEqualTo("7")
    }

    @Test
    fun getClientServicesByDate_validRequest_returnsResponse() {
        given(serviceService.getClientServicesByDate(4L, LocalDate.of(2026, 2, 8))).willReturn(
            ClientServicesByDateResponse(
                clientId = 4L,
                services = listOf(ClientServicesByDateResponse.ClientServicesByDateEntry(1, "08:00 - 09:00", "M. Mustermann"))
            )
        )

        val result = mockMvc.post("/services/client-and-date") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"clientId":4,"date":"2026-02-08"}"""
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"timepoint\":\"08:00 - 09:00\"")
        assertThat(result.response.contentAsString).contains("\"employeeName\":\"M. Mustermann\"")
    }

    @Test
    fun getClientServicesByDate_serviceThrows_returnsBadRequest() {
        given(serviceService.getClientServicesByDate(4L, LocalDate.of(2026, 2, 8)))
            .willThrow(IllegalStateException("boom"))

        val result = mockMvc.post("/services/client-and-date") {
            contentType = MediaType.APPLICATION_JSON
            content = ClientServicesByDateRequest(4L, LocalDate.of(2026, 2, 8)).let { """{"clientId":4,"date":"2026-02-08"}""" }
        }.andReturn()

        assertThat(result.response.status).isEqualTo(400)
    }
}
