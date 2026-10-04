package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dto.ClientArchiveHistoryEntryResponse
import de.vinz.openfls.domains.clients.dto.ClientArchiveHistoryResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveResult
import de.vinz.openfls.domains.clients.entity.ClientArchiveActionType
import de.vinz.openfls.domains.clients.service.ClientArchiveService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
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

@WebMvcTest(ClientArchiveController::class)
@AutoConfigureMockMvc(addFilters = false)
class ClientArchiveControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientArchiveService: ClientArchiveService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    private val archiveDate = LocalDate.of(2026, 5, 23)

    @Test
    fun getArchiveHistory_returnsEntriesInGivenOrder() {
        // Given
        val newest = entry(2L, ClientArchiveActionType.REACTIVATE, LocalDateTime.of(2026, 5, 24, 10, 45))
        val older = entry(1L, ClientArchiveActionType.ARCHIVE, LocalDateTime.of(2026, 5, 23, 10, 30))
        given(clientArchiveService.getHistory(17L)).willReturn(ClientArchiveHistoryResult.Success(listOf(newest, older)))

        // When
        val result = mockMvc.get("/clients/17/archive/history").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString.indexOf("\"id\":2")).isLessThan(
            result.response.contentAsString.indexOf("\"id\":1")
        )
    }

    @Test
    fun getArchiveHistory_withoutPermission_returnsForbidden() {
        given(clientArchiveService.getHistory(17L)).willReturn(ClientArchiveHistoryResult.Forbidden)

        assertThat(mockMvc.get("/clients/17/archive/history").andReturn().response.status).isEqualTo(403)
    }

    @Test
    fun getArchiveHistory_unknownClient_returnsNotFound() {
        given(clientArchiveService.getHistory(17L)).willReturn(ClientArchiveHistoryResult.NotFound)

        assertThat(mockMvc.get("/clients/17/archive/history").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun archive_withPermission_returnsHistoryEntry() {
        // Given
        given(clientArchiveService.archive(17L, archiveDate, "Client requested archive", "Initial archive")).willReturn(
            ClientArchiveResult.Success(entry(19L, ClientArchiveActionType.ARCHIVE, LocalDateTime.of(2026, 5, 23, 10, 30)))
        )

        // When
        val result = postJson("/clients/17/archive")

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":19")
        assertThat(result.response.contentAsString).contains("\"actionType\":\"ARCHIVE\"")
    }

    @Test
    fun archive_withoutPermission_returnsForbidden() {
        given(clientArchiveService.archive(17L, archiveDate, "Client requested archive", "Initial archive"))
            .willReturn(ClientArchiveResult.Forbidden)

        assertThat(postJson("/clients/17/archive").response.status).isEqualTo(403)
    }

    @Test
    fun archive_alreadyArchived_returnsConflict() {
        given(clientArchiveService.archive(17L, archiveDate, "Client requested archive", "Initial archive"))
            .willReturn(ClientArchiveResult.AlreadyArchived)

        assertThat(postJson("/clients/17/archive").response.status).isEqualTo(409)
    }

    @Test
    fun archive_unknownClient_returnsNotFound() {
        given(clientArchiveService.archive(17L, archiveDate, "Client requested archive", "Initial archive"))
            .willReturn(ClientArchiveResult.NotFound)

        assertThat(postJson("/clients/17/archive").response.status).isEqualTo(404)
    }

    @Test
    fun reactivate_notArchived_returnsConflict() {
        given(clientArchiveService.reactivate(17L, archiveDate, "Client requested archive", "Initial archive"))
            .willReturn(ClientArchiveResult.NotArchived)

        assertThat(postJson("/clients/17/reactivate").response.status).isEqualTo(409)
    }

    private fun postJson(url: String) = mockMvc.post(url) {
        contentType = MediaType.APPLICATION_JSON
        content = """{"actionDate":"2026-05-23","reason":"Client requested archive","remark":"Initial archive"}"""
    }.andReturn()

    private fun entry(id: Long, actionType: ClientArchiveActionType, timestamp: LocalDateTime) =
        ClientArchiveHistoryEntryResponse(
            id = id,
            actionType = actionType,
            actionDate = timestamp.toLocalDate(),
            actionTimestamp = timestamp,
            reason = "Reason",
            remark = "Remark",
            executingEmployeeId = 8L,
            executingEmployeeFirstname = "Anna",
            executingEmployeeLastname = "Lead"
        )
}
