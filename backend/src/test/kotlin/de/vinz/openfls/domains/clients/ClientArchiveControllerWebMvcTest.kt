package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.archive.ClientArchiveActionRequest
import de.vinz.openfls.domains.clients.archive.ClientArchiveService
import de.vinz.openfls.domains.clients.archive.ClientArchiveStateException
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportService
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportStateException
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportDownloadLinkDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportRequestDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportStatusDto
import de.vinz.openfls.domains.clients.archive.dtos.ClientArchiveHistoryEntryDto
import de.vinz.openfls.domains.clients.archive.dtos.ClientArchiveHistoryEntryReadDto
import de.vinz.openfls.domains.employees.dtos.EmployeeDto
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
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

@WebMvcTest(ClientArchiveController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class ClientArchiveControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientArchiveService: ClientArchiveService

    @MockitoBean
    lateinit var clientArchiveExportService: ClientArchiveExportService

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun getArchiveHistory_returnsReadDtosInNewestFirstOrder() {
        // Given
        val clientId = 17L
        val newest = ClientArchiveHistoryEntryReadDto().apply {
            id = 2L
            actionType = de.vinz.openfls.domains.clients.archive.ClientArchiveActionType.REACTIVATE
            actionDate = LocalDate.of(2026, 5, 24)
            actionTimestamp = LocalDateTime.of(2026, 5, 24, 10, 45)
            reason = "Reactivated"
            remark = "Back to active"
            executingEmployeeId = 8L
            executingEmployeeFirstname = "Anna"
            executingEmployeeLastname = "Lead"
        }
        val older = ClientArchiveHistoryEntryReadDto().apply {
            id = 1L
            actionType = de.vinz.openfls.domains.clients.archive.ClientArchiveActionType.ARCHIVE
            actionDate = LocalDate.of(2026, 5, 23)
            actionTimestamp = LocalDateTime.of(2026, 5, 23, 10, 30)
            reason = "Archived by request"
            remark = "Initial archive"
            executingEmployeeId = 8L
            executingEmployeeFirstname = "Anna"
            executingEmployeeLastname = "Lead"
        }
        given(clientArchiveService.getArchiveHistory(clientId)).willReturn(listOf(newest, older))

        // When
        val result = mockMvc.get("/clients/$clientId/archive/history").andReturn()

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
        val clientId = 17L
        val archiveDate = LocalDate.of(2026, 5, 23)
        val employeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = employeeId
            firstName = "Anna"
            lastName = "Lead"
        }
        val entry = ClientArchiveHistoryEntryDto().apply {
            id = 19L
            actionType = de.vinz.openfls.domains.clients.archive.ClientArchiveActionType.ARCHIVE
            actionDate = archiveDate
            actionTimestamp = LocalDateTime.of(2026, 5, 23, 10, 30)
            reason = "Client requested archive"
            remark = "Initial archive"
            executingEmployeeId = employeeId
            executingEmployeeFirstname = "Anna"
            executingEmployeeLastname = "Lead"
        }
        given(accessService.getId()).willReturn(employeeId)
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.getLeadingInstitutionIds()).willReturn(listOf(3L))
        given(employeeService.getEmployeeDtoById(employeeId, false)).willReturn(employeeDto)
        given(
            clientArchiveService.archive(
                eq(clientId),
                eq(archiveDate),
                eq("Client requested archive"),
                eq("Initial archive"),
                any()
            )
        ).willReturn(entry)

        // When
        val result = mockMvc.post("/clients/$clientId/archive") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "actionDate": "2026-05-23",
                  "reason": "Client requested archive",
                  "remark": "Initial archive"
                }
            """.trimIndent()
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":19")
        assertThat(result.response.contentAsString).contains("\"reason\":\"Client requested archive\"")
    }

    @Test
    fun archive_withoutPermission_returnsForbidden() {
        // Given
        val clientId = 17L
        val employeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = employeeId
            firstName = "Anna"
            lastName = "Employee"
        }
        given(accessService.getId()).willReturn(employeeId)
        given(accessService.isAdmin()).willReturn(false)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(employeeService.getEmployeeDtoById(employeeId, false)).willReturn(employeeDto)
        given(
            clientArchiveService.archive(
                eq(clientId),
                eq(LocalDate.of(2026, 5, 23)),
                eq("Client requested archive"),
                eq("Initial archive"),
                any()
            )
        ).willThrow(de.vinz.openfls.exceptions.UserNotAllowedException())

        // When
        val result = mockMvc.post("/clients/$clientId/archive") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "actionDate": "2026-05-23",
                  "reason": "Client requested archive",
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
        val clientId = 17L
        val employeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = employeeId
            firstName = "Anna"
            lastName = "Lead"
        }
        given(accessService.getId()).willReturn(employeeId)
        given(accessService.isAdmin()).willReturn(true)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(employeeService.getEmployeeDtoById(employeeId, true)).willReturn(employeeDto)
        given(
            clientArchiveService.reactivate(
                eq(clientId),
                eq(LocalDate.of(2026, 5, 23)),
                eq("Client is active again"),
                eq("Reactivated"),
                any()
            )
        ).willThrow(ClientArchiveStateException("client is not archived"))

        // When
        val result = mockMvc.post("/clients/$clientId/reactivate") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "actionDate": "2026-05-23",
                  "reason": "Client is active again",
                  "remark": "Reactivated"
                }
            """.trimIndent()
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(409)
    }

    @Test
    fun requestExport_withPermission_returnsDownloadStatus() {
        // Given
        val clientId = 17L
        val employeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = employeeId
            firstName = "Anna"
            lastName = "Lead"
        }
        val downloadLink = ClientArchiveExportDownloadLinkDto().apply {
            this.downloadLink = "/clients/$clientId/archive/export/token-1"
            downloadLinkExpiresAt = LocalDateTime.of(2026, 6, 13, 12, 0)
        }
        val status = ClientArchiveExportStatusDto().apply {
            ready = true
            format = ClientArchiveExportFormat.JSON
            requestedAt = LocalDateTime.of(2026, 6, 13, 11, 15)
            requestedByEmployeeId = employeeId
            this.downloadLink = downloadLink
        }
        given(accessService.getId()).willReturn(employeeId)
        given(accessService.isAdmin()).willReturn(true)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(employeeService.getEmployeeDtoById(employeeId, true)).willReturn(employeeDto)
        given(
            clientArchiveExportService.requestExport(
                eq(clientId),
                eq(ClientArchiveExportFormat.JSON),
                any()
            )
        ).willReturn(status)

        // When
        val result = mockMvc.post("/clients/$clientId/archive/export") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"format":"JSON"}"""
        }.andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"downloadLink\":\"/clients/$clientId/archive/export/token-1\"")
    }

    @Test
    fun getExportStatus_withPermission_returnsCurrentLink() {
        // Given
        val clientId = 17L
        val employeeId = 8L
        val employeeDto = EmployeeDto().apply {
            id = employeeId
            firstName = "Anna"
            lastName = "Lead"
        }
        val downloadLink = ClientArchiveExportDownloadLinkDto().apply {
            this.downloadLink = "/clients/$clientId/archive/export/token-1"
            downloadLinkExpiresAt = LocalDateTime.of(2026, 6, 13, 12, 0)
        }
        val status = ClientArchiveExportStatusDto().apply {
            ready = true
            format = ClientArchiveExportFormat.JSON
            requestedAt = LocalDateTime.of(2026, 6, 13, 11, 15)
            requestedByEmployeeId = employeeId
            this.downloadLink = downloadLink
        }
        given(accessService.getId()).willReturn(employeeId)
        given(accessService.isAdmin()).willReturn(true)
        given(accessService.getLeadingInstitutionIds()).willReturn(emptyList())
        given(employeeService.getEmployeeDtoById(employeeId, true)).willReturn(employeeDto)
        given(clientArchiveExportService.getExportStatus(eq(clientId), any())).willReturn(status)

        // When
        val result = mockMvc.get("/clients/$clientId/archive/export").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"ready\":true")
    }

    @Test
    fun downloadExport_withExpiredToken_returnsGone() {
        // Given
        val clientId = 17L
        val downloadToken = "token-1"
        given(
            clientArchiveExportService.downloadExport(
                clientId,
                downloadToken
            )
        ).willThrow(ClientArchiveExportStateException("export unavailable"))

        // When
        val result = mockMvc.get("/clients/$clientId/archive/export/$downloadToken").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(410)
    }

}
