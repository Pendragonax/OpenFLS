package de.vinz.openfls.domains.clients.archive.export

import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportDownloadLinkDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportRequestDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportStatusDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ClientArchiveExportContractTest {

    @Test
    fun requestDto_defaultFormat_isJson() {
        // Given
        val request = ClientArchiveExportRequestDto()

        // Then
        assertThat(request.format).isEqualTo(ClientArchiveExportFormat.JSON)
    }

    @Test
    fun statusDto_withDownloadLink_keepsReadinessAndMetadata() {
        // Given
        val downloadLink = ClientArchiveExportDownloadLinkDto().apply {
            this.downloadLink = "/exports/client/17.json"
            downloadLinkExpiresAt = LocalDateTime.of(2026, 6, 13, 12, 0)
            downloadedAt = LocalDateTime.of(2026, 6, 13, 11, 45)
        }
        val status = ClientArchiveExportStatusDto().apply {
            ready = true
            format = ClientArchiveExportFormat.JSON
            requestedAt = LocalDateTime.of(2026, 6, 13, 11, 15)
            requestedByEmployeeId = 8
            this.downloadLink = downloadLink
        }

        // Then
        assertThat(status.ready).isTrue
        assertThat(status.format).isEqualTo(ClientArchiveExportFormat.JSON)
        assertThat(status.downloadLink).isNotNull
        assertThat(status.downloadLink!!.downloadLink).isEqualTo("/exports/client/17.json")
        assertThat(status.downloadLink!!.downloadLinkExpiresAt).isEqualTo(LocalDateTime.of(2026, 6, 13, 12, 0))
        assertThat(status.downloadLink!!.downloadedAt).isEqualTo(LocalDateTime.of(2026, 6, 13, 11, 45))
    }
}
