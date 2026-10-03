package de.vinz.openfls.domains.clients.dto

import de.vinz.openfls.domains.clients.entity.ClientArchiveActionType
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.entity.ClientArchiveHistoryEntry
import java.time.LocalDate
import java.time.LocalDateTime

data class ClientArchiveHistoryEntryResponse(
    val id: Long = 0,
    val actionType: ClientArchiveActionType = ClientArchiveActionType.ARCHIVE,
    val exportFormat: ClientArchiveExportFormat? = null,
    val actionDate: LocalDate = LocalDate.now(),
    val actionTimestamp: LocalDateTime = LocalDateTime.now(),
    val reason: String = "",
    val remark: String = "",
    val executingEmployeeId: Long = 0,
    val executingEmployeeFirstname: String = "",
    val executingEmployeeLastname: String = ""
) {
    companion object {
        fun from(entry: ClientArchiveHistoryEntry): ClientArchiveHistoryEntryResponse {
            return ClientArchiveHistoryEntryResponse(
                id = entry.id,
                actionType = entry.actionType,
                exportFormat = entry.exportFormat,
                actionDate = entry.actionDate,
                actionTimestamp = entry.actionTimestamp,
                reason = entry.reason,
                remark = entry.remark,
                executingEmployeeId = entry.executingEmployeeId,
                executingEmployeeFirstname = entry.executingEmployeeFirstname,
                executingEmployeeLastname = entry.executingEmployeeLastname
            )
        }
    }
}
