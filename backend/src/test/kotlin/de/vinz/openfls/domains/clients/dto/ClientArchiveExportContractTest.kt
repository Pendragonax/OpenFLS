package de.vinz.openfls.domains.clients.dto

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ClientArchiveExportContractTest {

    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Test
    fun request_defaultFormat_isJson() {
        assertThat(ClientArchiveExportRequest().format).isEqualTo(ClientArchiveExportFormat.JSON)
    }

    @Test
    fun request_withoutAnonymize_isNotAnonymized() {
        val request = objectMapper.readValue("""{"format":"JSON"}""", ClientArchiveExportRequest::class.java)

        assertThat(request.anonymize).isFalse()
    }

    @Test
    fun status_withDownloadLink_serializesReadinessAndMetadata() {
        // Given
        val status = ClientArchiveExportStatusResponse(
            ready = true,
            format = ClientArchiveExportFormat.JSON,
            requestedAt = LocalDateTime.of(2026, 6, 13, 11, 15),
            requestedByEmployeeId = 8,
            downloadLink = ClientArchiveExportDownloadLinkResponse(
                downloadLink = "/exports/client/17.json",
                downloadLinkExpiresAt = LocalDateTime.of(2026, 6, 13, 12, 0),
                downloadedAt = LocalDateTime.of(2026, 6, 13, 11, 45)
            )
        )

        // When
        val json = objectMapper.readTree(objectMapper.writeValueAsString(status))

        // Then
        assertThat(json["ready"].asBoolean()).isTrue
        assertThat(json["format"].asText()).isEqualTo("JSON")
        assertThat(json["requestedAt"].asText()).isEqualTo("2026-06-13T11:15:00")
        assertThat(json["requestedByEmployeeId"].asLong()).isEqualTo(8)
        assertThat(json["downloadLink"]["downloadLink"].asText()).isEqualTo("/exports/client/17.json")
        assertThat(json["downloadLink"]["downloadLinkExpiresAt"].asText()).isEqualTo("2026-06-13T12:00:00")
        assertThat(json["downloadLink"]["downloadedAt"].asText()).isEqualTo("2026-06-13T11:45:00")
    }
}
