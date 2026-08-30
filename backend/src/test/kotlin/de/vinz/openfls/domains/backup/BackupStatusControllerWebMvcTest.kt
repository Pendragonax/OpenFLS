package de.vinz.openfls.domains.backup

import de.vinz.openfls.domains.backup.controller.BackupStatusController
import de.vinz.openfls.domains.backup.dto.BackupHistoryEntryDto
import de.vinz.openfls.domains.backup.dto.BackupRunDto
import de.vinz.openfls.domains.backup.dto.BackupStatusDto
import de.vinz.openfls.domains.backup.service.BackupStatusService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(BackupStatusController::class, properties = ["logging.performance=false"])
@AutoConfigureMockMvc(addFilters = false)
class BackupStatusControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var backupStatusService: BackupStatusService

    @Test
    fun status_returnsMappedDto() {
        given(backupStatusService.status()).willReturn(
            BackupStatusDto(
                lastBackup = BackupRunDto("2026-08-30T02:00:00.000Z", "success", "ok", "b.sql.gz", 123, "hash", 13, null),
                lastRestoreTest = null,
                backupOverdue = false,
                maxAgeHours = 7,
                overall = "ok",
                config = null
            )
        )

        val result = mockMvc.get("/admin/backup/status").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"overall\":\"ok\"")
        assertThat(result.response.contentAsString).contains("\"sizeBytes\":123")
        assertThat(result.response.contentAsString).contains("\"backupOverdue\":false")
    }

    @Test
    fun history_defaultLimitIsHundred() {
        given(backupStatusService.history(100)).willReturn(
            listOf(BackupHistoryEntryDto("backup", "2026-08-30T02:00:00.000Z", "success", "ok", "b.sql.gz", 1, "h", 1))
        )

        val result = mockMvc.get("/admin/backup/history").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"kind\":\"backup\"")
        verify(backupStatusService).history(100)
    }

    @Test
    fun history_passesExplicitLimit() {
        given(backupStatusService.history(25)).willReturn(emptyList())

        val result = mockMvc.get("/admin/backup/history?limit=25").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        verify(backupStatusService).history(25)
    }
}
