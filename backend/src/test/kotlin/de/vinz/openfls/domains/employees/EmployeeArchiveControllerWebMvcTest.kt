package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionRequest
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActor
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveService
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveStateException
import de.vinz.openfls.domains.employees.archive.dtos.EmployeeArchiveHistoryEntryDto
import de.vinz.openfls.domains.employees.archive.dtos.EmployeeArchiveHistoryEntryReadDto
import de.vinz.openfls.domains.employees.dtos.EmployeeDto
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.exceptions.UserNotAllowedException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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

@WebMvcTest(EmployeeArchiveController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class EmployeeArchiveControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var employeeArchiveService: EmployeeArchiveService

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun getArchiveHistory_returnsReadDtosInNewestFirstOrder() {
        // Given
        val employeeId = 17L
        val newest = EmployeeArchiveHistoryEntryReadDto().apply {
            id = 2L
            actionType = de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionType.REACTIVATE
            actionDate = LocalDate.of(2026, 7, 4)
            actionTimestamp = LocalDateTime.of(2026, 7, 4, 10, 45)
            reason = "Reactivated"
            remark = "Back to active"
            executingEmployeeId = 8L
            executingEmployeeFirstname = "Anna"
            executingEmployeeLastname = "Lead"
        }
        val older = EmployeeArchiveHistoryEntryReadDto().apply {
            id = 1L
            actionType = de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionType.ARCHIVE
            actionDate = LocalDate.of(2026, 7, 3)
            actionTimestamp = LocalDateTime.of(2026, 7, 3, 10, 30)
            reason = "Archived by request"
            remark = "Initial archive"
            executingEmployeeId = 8L
            executingEmployeeFirstname = "Anna"
            executingEmployeeLastname = "Lead"
        }
        given(employeeArchiveService.getArchiveHistory(employeeId)).willReturn(listOf(newest, older))

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
    fun archive_withPermission_returnsHistoryEntry() {
        // Given
        val employeeId = 17L
        val requestEmployeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = requestEmployeeId
            firstName = "Anna"
            lastName = "Lead"
        }
        val entry = EmployeeArchiveHistoryEntryDto().apply {
            id = 19L
            actionType = de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionType.ARCHIVE
            actionDate = LocalDate.of(2026, 7, 4)
            actionTimestamp = LocalDateTime.of(2026, 7, 4, 10, 30)
            reason = "Employee archived"
            remark = "Initial archive"
            executingEmployeeId = requestEmployeeId
            executingEmployeeFirstname = "Anna"
            executingEmployeeLastname = "Lead"
        }
        given(accessService.getId()).willReturn(requestEmployeeId)
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.getEmployeeDtoById(requestEmployeeId, true)).willReturn(employeeDto)
        given(
            employeeArchiveService.archive(
                eq(employeeId),
                eq(LocalDate.of(2026, 7, 4)),
                eq("Employee archived"),
                eq("Initial archive"),
                any()
            )
        ).willReturn(entry)

        // When
        val result = mockMvc.post("/employees/$employeeId/archive") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "actionDate": "2026-07-04",
                  "reason": "Employee archived",
                  "remark": "Initial archive"
                }
            """.trimIndent()
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":19")
        assertThat(result.response.contentAsString).contains("\"reason\":\"Employee archived\"")
    }

    @Test
    fun archive_withoutPermission_returnsForbidden() {
        // Given
        val employeeId = 17L
        val requestEmployeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = requestEmployeeId
            firstName = "Anna"
            lastName = "Employee"
        }
        given(accessService.getId()).willReturn(requestEmployeeId)
        given(accessService.isAdmin()).willReturn(false)
        given(employeeService.getEmployeeDtoById(requestEmployeeId, false)).willReturn(employeeDto)
        given(
            employeeArchiveService.archive(
                eq(employeeId),
                eq(LocalDate.of(2026, 7, 4)),
                eq("Employee archived"),
                eq("Initial archive"),
                any()
            )
        ).willThrow(UserNotAllowedException())

        // When
        val result = mockMvc.post("/employees/$employeeId/archive") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "actionDate": "2026-07-04",
                  "reason": "Employee archived",
                  "remark": "Initial archive"
                }
            """.trimIndent()
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(403)
    }

    @Test
    fun reactivate_duplicateState_returnsConflict() {
        // Given
        val employeeId = 17L
        val requestEmployeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = requestEmployeeId
            firstName = "Anna"
            lastName = "Lead"
        }
        given(accessService.getId()).willReturn(requestEmployeeId)
        given(accessService.isAdmin()).willReturn(true)
        given(employeeService.getEmployeeDtoById(requestEmployeeId, true)).willReturn(employeeDto)
        given(
            employeeArchiveService.reactivate(
                eq(employeeId),
                eq(LocalDate.of(2026, 7, 4)),
                eq("Employee active again"),
                eq("Reactivated"),
                any()
            )
        ).willThrow(EmployeeArchiveStateException("employee is not archived"))

        // When
        val result = mockMvc.post("/employees/$employeeId/reactivate") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "actionDate": "2026-07-04",
                  "reason": "Employee active again",
                  "remark": "Reactivated"
                }
            """.trimIndent()
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(409)
    }
}
