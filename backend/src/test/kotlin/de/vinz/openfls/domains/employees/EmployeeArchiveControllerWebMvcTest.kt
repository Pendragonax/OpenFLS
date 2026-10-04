package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.dto.EmployeeArchiveHistoryEntryResponse
import de.vinz.openfls.domains.employees.dto.EmployeeArchiveResult
import de.vinz.openfls.domains.employees.entity.EmployeeArchiveActionType
import de.vinz.openfls.domains.employees.service.EmployeeArchiveService
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(EmployeeArchiveController::class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeArchiveControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var employeeArchiveService: EmployeeArchiveService

    @MockitoBean
    lateinit var accessService: AccessService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    private val archiveJson = """
        {
          "actionDate": "2026-07-04",
          "reason": "Employee archived",
          "remark": "Initial archive"
        }
    """.trimIndent()

    @Test
    fun getArchiveHistory_admin_returnsEntriesInGivenOrder() {
        // Given
        val employeeId = 17L
        val newest = historyEntry(id = 2L, actionType = EmployeeArchiveActionType.REACTIVATE)
        val older = historyEntry(id = 1L, actionType = EmployeeArchiveActionType.ARCHIVE)
        given(accessService.isAdmin()).willReturn(true)
        given(employeeArchiveService.getHistory(employeeId)).willReturn(listOf(newest, older))

        // When
        val result = mockMvc.get("/employees/$employeeId/archive/history").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":2")
        assertThat(result.response.contentAsString).contains("\"id\":1")
        assertThat(result.response.contentAsString.indexOf("\"id\":2")).isLessThan(
            result.response.contentAsString.indexOf("\"id\":1")
        )
    }

    @Test
    fun getArchiveHistory_nonAdmin_returnsForbidden() {
        // Given
        given(accessService.isAdmin()).willReturn(false)

        // When
        val result = mockMvc.get("/employees/17/archive/history").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(employeeArchiveService, never()).getHistory(any())
    }

    @Test
    fun getArchiveHistory_unknownEmployee_returnsNotFound() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeArchiveService.getHistory(17L)).willReturn(null)

        // When
        val result = mockMvc.get("/employees/17/archive/history").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun archive_admin_returnsHistoryEntry() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeArchiveService.archive(17L, LocalDate.of(2026, 7, 4), "Employee archived", "Initial archive"))
            .willReturn(EmployeeArchiveResult.Success(historyEntry(id = 19L, reason = "Employee archived")))

        // When
        val result = postJson("/employees/17/archive")

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":19")
        assertThat(result.response.contentAsString).contains("\"reason\":\"Employee archived\"")
    }

    @Test
    fun archive_nonAdmin_returnsForbidden() {
        // Given
        given(accessService.isAdmin()).willReturn(false)

        // When
        val result = postJson("/employees/17/archive")

        // Then
        assertThat(result.response.status).isEqualTo(403)
        verify(employeeArchiveService, never()).archive(any(), any(), any(), any())
    }

    @Test
    fun archive_alreadyArchived_returnsConflict() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeArchiveService.archive(17L, LocalDate.of(2026, 7, 4), "Employee archived", "Initial archive"))
            .willReturn(EmployeeArchiveResult.AlreadyArchived)

        // When / Then
        assertThat(postJson("/employees/17/archive").response.status).isEqualTo(409)
    }

    @Test
    fun archive_unknownEmployee_returnsNotFound() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeArchiveService.archive(17L, LocalDate.of(2026, 7, 4), "Employee archived", "Initial archive"))
            .willReturn(EmployeeArchiveResult.NotFound)

        // When / Then
        assertThat(postJson("/employees/17/archive").response.status).isEqualTo(404)
    }

    @Test
    fun reactivate_notArchived_returnsConflict() {
        // Given
        given(accessService.isAdmin()).willReturn(true)
        given(employeeArchiveService.reactivate(17L, LocalDate.of(2026, 7, 4), "Employee archived", "Initial archive"))
            .willReturn(EmployeeArchiveResult.NotArchived)

        // When / Then
        assertThat(postJson("/employees/17/reactivate").response.status).isEqualTo(409)
    }

    @Test
    fun reactivate_nonAdmin_returnsForbidden() {
        // Given
        given(accessService.isAdmin()).willReturn(false)

        // When / Then
        assertThat(postJson("/employees/17/reactivate").response.status).isEqualTo(403)
    }

    private fun postJson(url: String) = mockMvc.post(url) {
        contentType = MediaType.APPLICATION_JSON
        content = archiveJson
    }.andReturn()

    private fun historyEntry(
        id: Long,
        actionType: EmployeeArchiveActionType = EmployeeArchiveActionType.ARCHIVE,
        reason: String = "Reason"
    ) = EmployeeArchiveHistoryEntryResponse(
        id = id,
        actionType = actionType,
        actionDate = LocalDate.of(2026, 7, 4),
        actionTimestamp = LocalDateTime.of(2026, 7, 4, 10, 30),
        reason = reason,
        remark = "Remark",
        executingEmployeeId = 8L,
        executingEmployeeFirstname = "Anna",
        executingEmployeeLastname = "Lead"
    )
}
