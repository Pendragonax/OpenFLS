package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dto.ClientArchiveExportDownloadDto
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportDownloadLinkResponse
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportDownloadResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportRequestResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportStatusResponse
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportStatusResult
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.service.ClientArchiveExportService
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
import java.time.LocalDateTime

@WebMvcTest(ClientArchiveExportController::class)
@AutoConfigureMockMvc(addFilters = false)
class ClientArchiveExportControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var clientArchiveExportService: ClientArchiveExportService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun requestExport_withPermission_returnsDownloadStatus() {
        given(clientArchiveExportService.requestExport(17L, ClientArchiveExportFormat.JSON, false))
            .willReturn(ClientArchiveExportRequestResult.Success(readyStatus(17L)))

        val result = postExport("""{"format":"JSON"}""")

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"downloadLink\":\"/clients/17/archive/export/token-1\"")
    }

    @Test
    fun requestExport_withAnonymizeTrue_forwardsFlagToService() {
        given(clientArchiveExportService.requestExport(17L, ClientArchiveExportFormat.JSON, true))
            .willReturn(ClientArchiveExportRequestResult.Success(readyStatus(17L)))

        val result = postExport("""{"format":"JSON","anonymize":true}""")

        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun requestExport_withoutPermission_returnsForbidden() {
        given(clientArchiveExportService.requestExport(17L, ClientArchiveExportFormat.JSON, false))
            .willReturn(ClientArchiveExportRequestResult.Forbidden)

        assertThat(postExport("""{"format":"JSON"}""").response.status).isEqualTo(403)
    }

    @Test
    fun requestExport_unknownClient_returnsNotFound() {
        given(clientArchiveExportService.requestExport(17L, ClientArchiveExportFormat.JSON, false))
            .willReturn(ClientArchiveExportRequestResult.NotFound)

        assertThat(postExport("""{"format":"JSON"}""").response.status).isEqualTo(404)
    }

    @Test
    fun getExportStatus_withPermission_returnsCurrentLink() {
        given(clientArchiveExportService.getExportStatus(17L))
            .willReturn(ClientArchiveExportStatusResult.Success(readyStatus(17L)))

        val result = mockMvc.get("/clients/17/archive/export").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"ready\":true")
    }

    @Test
    fun getExportStatus_withoutPermission_returnsForbidden() {
        given(clientArchiveExportService.getExportStatus(17L)).willReturn(ClientArchiveExportStatusResult.Forbidden)

        assertThat(mockMvc.get("/clients/17/archive/export").andReturn().response.status).isEqualTo(403)
    }

    @Test
    fun downloadExport_withValidToken_returnsTheFile() {
        given(clientArchiveExportService.downloadExport(17L, "token-1")).willReturn(
            ClientArchiveExportDownloadResult.Success(
                ClientArchiveExportDownloadDto(fileName = "export.json", content = "{}".toByteArray())
            )
        )

        val result = mockMvc.get("/clients/17/archive/export/token-1").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.getHeader("Content-Disposition")).isEqualTo("attachment; filename=export.json")
        assertThat(result.response.contentAsString).isEqualTo("{}")
    }

    @Test
    fun downloadExport_withExpiredToken_returnsGone() {
        given(clientArchiveExportService.downloadExport(17L, "expired-token"))
            .willReturn(ClientArchiveExportDownloadResult.Gone("export unavailable"))

        assertThat(mockMvc.get("/clients/17/archive/export/expired-token").andReturn().response.status).isEqualTo(410)
    }

    private fun postExport(body: String) = mockMvc.post("/clients/17/archive/export") {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }.andReturn()

    private fun readyStatus(clientId: Long) = ClientArchiveExportStatusResponse(
        ready = true,
        format = ClientArchiveExportFormat.JSON,
        requestedAt = LocalDateTime.of(2026, 6, 13, 11, 15),
        requestedByEmployeeId = 8L,
        downloadLink = ClientArchiveExportDownloadLinkResponse(
            downloadLink = "/clients/$clientId/archive/export/token-1",
            downloadLinkExpiresAt = LocalDateTime.of(2026, 6, 13, 11, 35)
        )
    )
}
