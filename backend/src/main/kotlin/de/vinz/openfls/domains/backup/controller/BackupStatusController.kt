package de.vinz.openfls.domains.backup.controller

import de.vinz.openfls.domains.backup.dto.BackupHistoryEntryDto
import de.vinz.openfls.domains.backup.dto.BackupStatusDto
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
    fun status(): BackupStatusDto =
        backupStatusService.status().also { StructuredLog.audit("backup.status.read", "success") }

    @GetMapping("/history")
    fun history(@RequestParam(defaultValue = "100") limit: Int): List<BackupHistoryEntryDto> =
        backupStatusService.history(limit).also { StructuredLog.audit("backup.history.read", "success") }
}
