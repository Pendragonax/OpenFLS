package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanDeleteResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanEditResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateResult
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.permissions.service.AccessService
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

@WebMvcTest(AssistancePlanController::class)
@AutoConfigureMockMvc(addFilters = false)
class AssistancePlanControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun create_validRequest_returnsAssistancePlan() {
        given(assistancePlanService.create(any())).willReturn(AssistancePlanCreateResult.Success(planResponse(5L)))

        val result = postCreate()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":5")
    }

    @Test
    fun create_archivedClient_returnsConflict() {
        given(assistancePlanService.create(any())).willReturn(AssistancePlanCreateResult.ClientArchived)

        assertThat(postCreate().response.status).isEqualTo(409)
    }

    @Test
    fun create_invalidHours_returnsBadRequestWithReason() {
        given(assistancePlanService.create(any())).willReturn(AssistancePlanCreateResult.InvalidHours("not both"))

        val result = postCreate()

        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).isEqualTo("not both")
    }

    @Test
    fun create_unknownSponsor_returnsBadRequest() {
        given(assistancePlanService.create(any())).willReturn(AssistancePlanCreateResult.SponsorNotFound)

        assertThat(postCreate().response.status).isEqualTo(400)
    }

    @Test
    fun update_withoutPermission_returnsForbidden() {
        given(accessService.canModifyAssistancePlan(12L)).willReturn(false)

        val result = putUpdate(pathId = 12L, bodyId = 12L)

        assertThat(result.response.status).isEqualTo(403)
        verify(assistancePlanService, never()).update(any(), any())
    }

    @Test
    fun update_differentPathAndBodyId_returnsBadRequest() {
        given(accessService.canModifyAssistancePlan(12L)).willReturn(true)

        assertThat(putUpdate(pathId = 12L, bodyId = 13L).response.status).isEqualTo(400)
    }

    @Test
    fun update_unknownAssistancePlan_returnsNotFound() {
        given(accessService.canModifyAssistancePlan(12L)).willReturn(true)
        given(assistancePlanService.update(any(), any())).willReturn(AssistancePlanUpdateResult.NotFound)

        assertThat(putUpdate(pathId = 12L, bodyId = 12L).response.status).isEqualTo(404)
    }

    @Test
    fun update_hourModeChanged_returnsConflict() {
        given(accessService.canModifyAssistancePlan(12L)).willReturn(true)
        given(assistancePlanService.update(any(), any())).willReturn(AssistancePlanUpdateResult.HourModeChanged)

        assertThat(putUpdate(pathId = 12L, bodyId = 12L).response.status).isEqualTo(409)
    }

    @Test
    fun update_validRequest_returnsAssistancePlan() {
        given(accessService.canModifyAssistancePlan(12L)).willReturn(true)
        given(assistancePlanService.update(any(), any())).willReturn(AssistancePlanUpdateResult.Success(planResponse(12L)))

        val result = putUpdate(pathId = 12L, bodyId = 12L)

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":12")
    }

    @Test
    fun delete_nonAdmin_returnsForbidden() {
        given(accessService.isAdmin()).willReturn(false)

        val result = mockMvc.delete("/assistance_plans/12").andReturn()

        assertThat(result.response.status).isEqualTo(403)
        verify(assistancePlanService, never()).delete(any())
    }

    @Test
    fun delete_admin_returnsDeletedAssistancePlan() {
        given(accessService.isAdmin()).willReturn(true)
        given(assistancePlanService.delete(12L)).willReturn(AssistancePlanDeleteResult.Success(planResponse(12L)))

        val result = mockMvc.delete("/assistance_plans/12").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":12")
    }

    @Test
    fun delete_unknownAssistancePlan_returnsNotFound() {
        given(accessService.isAdmin()).willReturn(true)
        given(assistancePlanService.delete(12L)).willReturn(AssistancePlanDeleteResult.NotFound)

        assertThat(mockMvc.delete("/assistance_plans/12").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getEditById_unknownOrHiddenAssistancePlan_returnsNotFound() {
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(assistancePlanService.getEditById(12L, false, emptyList())).willReturn(null)

        assertThat(mockMvc.get("/assistance_plans/12/edit").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getEditById_admin_returnsEditResponse() {
        given(accessService.isAdmin()).willReturn(true)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(assistancePlanService.getEditById(12L, true, emptyList()))
            .willReturn(AssistancePlanEditResponse().apply {
                id = 12L
                clientArchived = true
            })

        val result = mockMvc.get("/assistance_plans/12/edit").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":12")
        assertThat(result.response.contentAsString).contains("\"clientArchived\":true")
    }

    @Test
    fun getDetailById_unknownAssistancePlan_returnsNotFound() {
        given(assistancePlanService.getDetailById(12L)).willReturn(null)

        assertThat(mockMvc.get("/assistance_plans/12/detail").andReturn().response.status).isEqualTo(404)
    }

    private fun postCreate() = mockMvc.post("/assistance_plans") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"start":"2026-01-01","end":"2026-12-31","clientId":1,"institutionId":2,"sponsorId":3}"""
    }.andReturn()

    private fun putUpdate(pathId: Long, bodyId: Long) = mockMvc.put("/assistance_plans/$pathId") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"id":$bodyId,"start":"2026-01-01","end":"2026-12-31","clientId":1,"institutionId":2,"sponsorId":3}"""
    }.andReturn()

    private fun planResponse(id: Long) = AssistancePlanResponse(
        id = id,
        start = LocalDate.of(2026, 1, 1),
        end = LocalDate.of(2026, 12, 31),
        clientId = 1,
        institutionId = 2,
        institutionName = "Schule",
        sponsorId = 3,
        hourMode = AssistancePlanHourMode.EXACT,
        hourCorridorId = 0,
        clientArchived = false
    )
}
