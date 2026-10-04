package de.vinz.openfls.domains.backup

import de.vinz.openfls.domains.backup.dto.BackupHistoryEntryResponse
import de.vinz.openfls.domains.backup.dto.BackupStatusResponse
import de.vinz.openfls.domains.backup.service.BackupStatusService
import de.vinz.openfls.logging.StructuredLog
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Read-only operations view of the database backup service. Secured by the
 * `/admin/backup` path rule requiring the `ADMIN` authority in
 * [de.vinz.openfls.security.SecurityConfiguration]. Both endpoints are
 * deliberately non-paginated with a fixed upper bound (see AGENTS.md).
 */
@RestController
@RequestMapping("/admin/backup")
class BackupStatusController(private val backupStatusService: BackupStatusService) {

    @GetMapping("/status")
    fun status(): BackupStatusResponse =
        backupStatusService.status().also { StructuredLog.audit("backup.status.read", "success") }

    @GetMapping("/history")
    fun history(@RequestParam(defaultValue = "100") limit: Int): List<BackupHistoryEntryResponse> =
        backupStatusService.history(limit).also { StructuredLog.audit("backup.history.read", "success") }
}
