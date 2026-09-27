package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.dtos.ContingentCreateResult
import de.vinz.openfls.domains.contingents.dtos.ContingentDeleteResult
import de.vinz.openfls.domains.contingents.dtos.ContingentResponse
import de.vinz.openfls.domains.contingents.dtos.ContingentUpdateResult
import de.vinz.openfls.domains.contingents.services.ContingentService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.kotlin.any
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

@WebMvcTest(ContingentController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class ContingentControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var contingentService: ContingentService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getByInstitutionId_adminWithIncludeArchived_forwardsOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(contingentService.getByInstitutionId(9L, true)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/institution/9") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByInstitutionId(9L, true)
    }

    @Test
    fun getByInstitutionId_nonAdminIgnoresOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(false)
        given(contingentService.getByInstitutionId(9L, false)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/institution/9") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByInstitutionId(9L, false)
    }

    @Test
    fun getByEmployeeId_withIncludeArchived_forwardsOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(contingentService.getByEmployeeId(7L, true)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/employee/7") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByEmployeeId(7L, true)
    }

    @Test
    fun getByEmployeeId_nonAdminIgnoresOptIn() {
        // Given
        given(accessService.isAdmin()).willReturn(false)
        given(contingentService.getByEmployeeId(7L, false)).willReturn(emptyList())

        // When
        val result = mockMvc.get("/contingents/employee/7") {
            param("includeArchivedEmployees", "true")
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        verify(contingentService).getByEmployeeId(7L, false)
    }

    @Test
    fun create_notLeaderOfInstitution_returnsForbidden() {
        // Given
        given(accessService.isLeader(5L)).willReturn(false)

        // When
        val result = mockMvc.post("/contingents") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"start":"2026-01-01","weeklyServiceHours":10.0,"employeeId":1,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(contingentService, never()).create(any())
    }

    @Test
    fun create_endBeforeStart_returnsBadRequest() {
        // Given
        given(accessService.isLeader(5L)).willReturn(true)
        given(contingentService.create(any())).willReturn(ContingentCreateResult.InvalidRange("end before start"))

        // When
        val result = mockMvc.post("/contingents") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"start":"2026-02-01","end":"2026-01-01","weeklyServiceHours":10.0,"employeeId":1,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.contentAsString).contains("end before start")
    }

    @Test
    fun create_admin_returnsCreatedDto() {
        // Given
        given(accessService.isLeader(5L)).willReturn(true)
        given(contingentService.create(any())).willReturn(
            ContingentCreateResult.Success(ContingentResponse(id = 3, employeeId = 1, institutionId = 5))
        )

        // When
        val result = mockMvc.post("/contingents") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"start":"2026-01-01","weeklyServiceHours":10.0,"employeeId":1,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun update_missingContingent_returnsNotFound() {
        // Given
        given(contingentService.getById(7L)).willReturn(null)

        // When
        val result = mockMvc.put("/contingents/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"start":"2026-01-01","weeklyServiceHours":10.0,"employeeId":1,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
        verify(contingentService, never()).update(any())
    }

    @Test
    fun update_pathIdDiffersFromRequestId_returnsBadRequest() {
        // When
        val result = mockMvc.put("/contingents/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":8,"start":"2026-01-01","weeklyServiceHours":10.0,"employeeId":1,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun update_notAllowedToModify_returnsForbidden() {
        // Given
        given(contingentService.getById(7L)).willReturn(ContingentResponse(id = 7, employeeId = 1, institutionId = 5))
        given(contingentService.canModifyContingent(7L)).willReturn(false)

        // When
        val result = mockMvc.put("/contingents/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"start":"2026-01-01","weeklyServiceHours":10.0,"employeeId":1,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(contingentService, never()).update(any())
    }

    @Test
    fun update_admin_returnsUpdatedDto() {
        // Given
        given(contingentService.getById(7L)).willReturn(ContingentResponse(id = 7, employeeId = 1, institutionId = 5))
        given(contingentService.canModifyContingent(7L)).willReturn(true)
        given(contingentService.update(any())).willReturn(
            ContingentUpdateResult.Success(ContingentResponse(id = 7, employeeId = 2, institutionId = 5))
        )

        // When
        val result = mockMvc.put("/contingents/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"start":"2026-01-01","weeklyServiceHours":10.0,"employeeId":2,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"employeeId\":2")
    }

    @Test
    fun update_serviceReportsNotFound_returnsNotFound() {
        // Given
        given(contingentService.getById(7L)).willReturn(ContingentResponse(id = 7, employeeId = 1, institutionId = 5))
        given(contingentService.canModifyContingent(7L)).willReturn(true)
        given(contingentService.update(any())).willReturn(ContingentUpdateResult.NotFound)

        // When
        val result = mockMvc.put("/contingents/7") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":7,"start":"2026-01-01","weeklyServiceHours":10.0,"employeeId":1,"institutionId":5}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun delete_missingContingent_returnsNotFound() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(contingentService.delete(7L)).willReturn(ContingentDeleteResult.NotFound)

        // When
        val result = mockMvc.delete("/contingents/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun delete_existingContingent_returnsDeletedDto() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(contingentService.delete(7L)).willReturn(
            ContingentDeleteResult.Success(ContingentResponse(id = 7, employeeId = 1, institutionId = 5))
        )

        // When
        val result = mockMvc.delete("/contingents/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":7")
    }

    @Test
    fun getById_missingContingent_returnsNotFound() {
        // Given
        given(contingentService.getById(7L)).willReturn(null)

        // When
        val result = mockMvc.get("/contingents/7").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }
}
